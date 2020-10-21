/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018 Psikoi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.kourendlibrary;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

@Singleton
class KourendLibraryPanel extends PluginPanel
{
	private static final ImageIcon RESET_ICON;
	private static final ImageIcon RESET_HOVER_ICON;

	private final KourendLibraryPlugin plugin;
	private final Library library;

	private final HashMap<Book, BookPanel> bookPanels = new HashMap<>();

	private Tower neT = new Tower(TowerDirection.NE);
	private Tower nwT = new Tower(TowerDirection.NW);
	private Tower swT = new Tower(TowerDirection.SW);
	private Tower midT = new Tower(TowerDirection.MID);

	static
	{
		final BufferedImage resetIcon = ImageUtil.getResourceStreamFromClass(KourendLibraryPanel.class, "/util/reset.png");
		RESET_ICON = new ImageIcon(resetIcon);
		RESET_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(resetIcon, -100));
	}

	@Inject
	KourendLibraryPanel(KourendLibraryPlugin plugin, Library library)
	{
		super();

		this.plugin = plugin;
		this.library = library;
	}

	void init()
	{
		setLayout(new BorderLayout(0, 5));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel books = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 4;

		Stream.of(Book.values())
			.filter(b -> !b.isDarkManuscript())
			.filter(b -> b != Book.VARLAMORE_ENVOY || plugin.showVarlamoreEnvoy())
			.sorted(Comparator.comparing(Book::getShortName))
			.forEach(b ->
			{
				BookPanel p = new BookPanel(b);
				bookPanels.put(b, p);
				books.add(p, c);
				c.gridy++;
			});
		c.gridy = 0;
		books.add(nwT.label, c);
		c.gridy++;
		books.add(neT.label, c);
		c.gridy++;
		books.add(swT.label, c);
		c.gridy++;
		books.add(midT.label, c);



		JButton reset = new JButton("Reset", RESET_ICON);
		reset.setRolloverIcon(RESET_HOVER_ICON);
		reset.addActionListener(ev ->
		{
			library.reset();
			update();
		});

		add(reset, BorderLayout.NORTH);
		add(books, BorderLayout.CENTER);
		update();
	}

	void update()
	{
		HashMap<TowerDirection,Tower> towers = new HashMap<TowerDirection,Tower>();
		neT.reset();
		nwT.reset();
		swT.reset();
		midT.reset();
		towers.put(TowerDirection.NE,neT);
		towers.put(TowerDirection.NW,nwT);
		towers.put(TowerDirection.SW,swT);
		towers.put(TowerDirection.MID,midT);

		SwingUtilities.invokeLater(() ->
		{
			Book customerBook = library.getCustomerBook();
			for (Map.Entry<Book, BookPanel> b : bookPanels.entrySet())
			{
				final Book book = b.getKey();
				final BookPanel panel = b.getValue();

				panel.setIsTarget(customerBook == book);
				panel.setIsHeld(plugin.doesPlayerContainBook(book));
			}

			HashMap<Book, HashSet<String>> bookLocations = new HashMap<>();

			for (Bookcase bookcase : library.getBookcases())
			{
				if (bookcase.getBook() != null)
				{
					bookLocations.computeIfAbsent(bookcase.getBook(), a -> new HashSet<>()).add(bookcase.getLocationString());
				}
				else
				{
					for (Book book : bookcase.getPossibleBooks())
					{
						if (book != null)
						{
							bookLocations.computeIfAbsent(book, a -> new HashSet<>()).add(bookcase.getLocationString());
						}
					}
				}
			}

			for (Map.Entry<Book, BookPanel> e : bookPanels.entrySet())
			{
				HashSet<String> locs = bookLocations.get(e.getKey());
				if (locs == null || locs.size() > 3)
				{
					e.getValue().setLocation("Unknown");
				}
				else
				{
					for(String loc : locs)
					{
						if(!e.getValue().getIsHeld()) {

							//Add book to counter if not currently held
							TowerDirection tKey = getDirectionFromString(loc);
							towers.get(tKey).addBook();

							if(e.getValue().getIsTarget()) {
								towers.get(tKey).setTarget(true);
							}
						}
					}
					e.getValue().setLocation("<html>" + locs.stream().collect(Collectors.joining("<br>")) + "</html>");
				}
			}
			List<Tower> towerVals = new ArrayList<Tower>(towers.values());
			Collections.sort(towerVals);

			for (int i = 0; i < towerVals.size(); i++) {
			    //Include towers tied for top 2 as yellow
                if (towerVals.get(i).compareTo(towerVals.get(2)) >= 0) {
                    TowerDirection dirKey = towerVals.get(i).getDirection();
                    towers.get(dirKey).setMost2(true);
                }
			}

		});
	}

	private TowerDirection getDirectionFromString(String direction)
	{
		if(direction.startsWith("Northeast"))
		{
			return TowerDirection.NE;
		}
		else if(direction.startsWith("Northwest"))
		{
			return TowerDirection.NW;
		}
		if(direction.startsWith("Southwest"))
		{
			return TowerDirection.SW;
		}
		else
		{
			return TowerDirection.MID;
		}
	}

	void reload()
	{
		bookPanels.clear();
		removeAll();
		init();
	}
}