///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.undo.*;

class SentenceMetrics {

	Font font;
	Font fontHighlight;
	FontMetrics fontMetrics;
	int fontHeight;
	int fontAscent;
	int fontDescent;
	String words[];
	int widths[];

	SentenceMetrics(FontMetrics fontMetrics, Font fontHighlight, String[] words) {
		// we can get the font from FontMetrics but not viceversa (except within a JComponent)
		this.fontMetrics = fontMetrics;
		this.font = fontMetrics.getFont();
		this.fontHighlight = fontHighlight;
		this.words = words;
		fontHeight = fontMetrics.getHeight();
		fontAscent = fontMetrics.getAscent();
		fontDescent = fontMetrics.getDescent();
		widths = new int[words.length];
		for (int i = 0; i < words.length; i++) {
			widths[i] = fontMetrics.stringWidth(words[i]);
		}
	}

	SentenceMetrics(FontMetrics fontMetrics, Font fontHighlight, Vector<String> vector) {
		this.fontMetrics = fontMetrics;
		this.fontHighlight = fontHighlight;
		this.font = fontMetrics.getFont();
		fontHeight = fontMetrics.getHeight();
		fontAscent = fontMetrics.getAscent();
		fontDescent = fontMetrics.getDescent();
		int n = vector.size();
		words = new String[n];
		widths = new int[n];
		for (int i = 0; i < n; i++) {
			words[i] = vector.get(i);
			widths[i] = fontMetrics.stringWidth(words[i]);
		}
	}

	/**
	 * Set word at position i to string s.
	 * @param i
	 * @param s
	 * @return width of word.
	 */
	public int wordAt(int i, String s) {
		words[i] = s;
		return widths[i] = fontMetrics.stringWidth(s);
	}

	public int Length() {
		return words.length;
	}
}

public class AnnotateCanvas extends TransPanel {

	static final Font wordFont = new Font("Serif", 0, 12);

	static final Font wordFontHighlight = new Font("Serif", Font.BOLD, 12);

	static final Font tagFont = new Font("SansSerif", Font.BOLD, 12);

	static final Font depsFont = new Font("SansSerif", Font.BOLD, 12);

	static final String DefaultDepType = "[  ]";

	static final Color selDepColor = new Color(240, 240, 160);

	static final Color selTagColor = new Color(240, 160, 240);

	static final Color selWordColor = new Color(160, 160, 240);

	static final Color highlitWordColor = new Color(0, 0, 200);

	static final Color highlitLabelColor = new Color(240, 24, 24);

	static final Color altArrowColor = new Color(240, 24, 24);

	static final Color noDepColor = new Color(0xDC, 0xDC, 0xDC);

	static final Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

	/// Undo support
	UndoManager undo = null;

	public void undo() {
		undo.undo();
		if (!sentence.changed())
			sentence.changed(undo.canUndo());
		frame.enableRedo(true);
		frame.enableUndo(undo.canUndo());
	}

	// FIXME: can only redo one action. Why?
	public void redo() {
		undo.redo();
		if (!sentence.changed())
			sentence.changed(true);
		frame.enableRedo(undo.canRedo());
		frame.enableUndo(true);
	}

	@SuppressWarnings("serial")
	class DgaEdit extends AbstractUndoableEdit {
		public void undo() {
			if (!sentence.changed()) {
				sentence.changed(true);	// FIXME: we might be back to a saved state
				frame.modelChanged();
			}
			frame.enableRedo(true);
			update();
		}
		public void redo() {
			if (!sentence.changed()) {
				sentence.changed(true);	// FIXME: we might be back to a saved state
				frame.modelChanged();
			}
			frame.enableUndo(undo.canUndo());
			update();
		}
	}

	public class DeleteArcEdit extends DgaEdit {
		public int from, to;
		public String label;

		DeleteArcEdit(int from, int to, String label) {
			this.from = from;
			this.to = to;
			this.label = label;
		}

		public void undo() {
			sentence.parents[from] = to;
			sentence.deps[from] = label;
			deps.wordAt(from, label);
			super.undo();
		}

		public void redo() {
			sentence.parents[from] = 0;
			sentence.deps[from] = "";
			super.redo();
		}

		private static final long serialVersionUID = 1L;
	}

	public class AddArcEdit extends DgaEdit {
		public int from, to;
		public String label;

		AddArcEdit(int from, int to, String label) {
			this.from = from;
			this.to = to;
			this.label = label;
		}

		public void undo() {
			sentence.parents[from] = 0;
			sentence.deps[from] = "";
			super.undo();
		}

		public void redo() {
			sentence.parents[from] = to;
			super.undo();
		}

		private static final long serialVersionUID = 1L;
	}

	public class LabelEdit extends DgaEdit {
		public int token;
		public String label, previous;

		LabelEdit(int token, String label, String previous) {
			this.token = token;
			this.label = label;
			this.previous = previous;
		}

		public void undo() {
			sentence.deps[token] = previous;
			deps.wordAt(token, previous);
			super.undo();
		}

		public void redo() {
			sentence.deps[token] = label; // model
			deps.wordAt(token, label);	// view
			super.redo();
		}

		private static final long serialVersionUID = 1L;
	}

	public class TagEdit extends DgaEdit {
		public int token;
		public String label, previous;

		TagEdit(int token, String label, String previous) {
			this.token = token;
			this.label = label;
			this.previous = previous;
		}

		public void undo() {
			sentence.tags[token] = previous;
			tags.wordAt(token, previous);
			super.undo();	
		}

		public void redo() {
			sentence.tags[token] = label; // model
			tags.wordAt(token, label);	// view
			super.redo();
		}

		private static final long serialVersionUID = 1L;
	}

	public class TokenEdit extends DgaEdit {
		int idx;
		String form;

		TokenEdit(int idx, String form) {
			this.idx = idx;
			this.form = form;
		}

		public void undo() {
			String temp = sentence.forms[idx];
			sentence.forms[idx] = form;
			words.widths[idx] = words.fontMetrics.stringWidth(form);
			tokenWidth[idx] = Math.max(tags.widths[idx], words.widths[idx]);	// reset in width
			form = temp;
			// refresh the Corpus pane
			frame.corpusPane.reset();
			super.undo();
		}

		public void redo() {
			String temp = sentence.forms[idx];
			sentence.forms[idx] = form;
			words.widths[idx] = words.fontMetrics.stringWidth(form);
			tokenWidth[idx] = Math.max(tags.widths[idx], words.widths[idx]);	// reset in width
			form = temp;
			// refresh the Corpus pane
			frame.corpusPane.reset();
			super.redo();	
		}

		private static final long serialVersionUID = 1L;
	}

	class Interactor extends AbstractPanInteractor {

		Point2D position = new Point2D.Double();
		Rectangle bounds = new Rectangle();

		/** Invoked when a mouse button is clicked (pressed and released) on a component. */
		public void mouseClicked(MouseEvent event) {
			requestFocusInWindow(true);
			if (!event.isPopupTrigger()
					&& SwingUtilities.isLeftMouseButton(event)
					&& sentence != null) {
				// find selection
				int x = (int)position.getX();
				int y = (int)position.getY();
				int yTag = getHeight() - padding;
				int yWord = yTag - tags.fontHeight - lineSpace;

				int selected = -1;
				if (yWord - words.fontHeight <= y && y <= yWord) {
					// on the words strip
					for (selected = 0; selected < length; selected++)
						if (wordX[selected] <= x
						&& x <= wordX[selected] + words.widths[selected])
							break;
				}
				if (0 <= selected && selected < length) {
					// word
					if (event.getClickCount() > 1) {
						// double click
						wordEdit.edit(canvas, event.getPoint(), selected);
						markWord(selected);
						unmarkWord();
					} else {
						// single click
						if (markWord >= 0) { // marked at previous Click
							if (markWord != selected) {
								// create link: store offset in
								// sentence.parents[]
								AddArcEdit ae = new AddArcEdit(markWord, selected - markWord, DefaultDepType);
								undo.addEdit(ae);
								ae.redo();
								depWord = markWord;
								unmarkWord();
							} else {
								// link to self, interpreted as sentence root
								AddArcEdit ae = new AddArcEdit(markWord, Integer.MAX_VALUE, DefaultDepType);
								undo.addEdit(ae);
								ae.redo();
								depWord = markWord;
								unmarkWord();
							}
						} else if (sentence.parents[selected] == 0) {
							markWord(selected);
							repaint();
						} else {
						  unmarkAll();
						  repaint();
						}
					}
				} else if (yTag - tags.fontHeight <= y && y <= yTag) {
					// on a tag
					for (int i = 0; i < length; i++) {
						if (tags.words[i].length() > 0 && tagX[i] <= x
								&& x <= tagX[i] + tags.widths[i]) {
							unmarkAll();
							tagWord = i;
							// show tag list
							annotator.showLeft("POS");
							repaint();
							break;
						}
					}
				} else if (highlitLabel != -1) {
					// selecting dependency link
					int save = highlitLabel;
					unmarkAll();
					depWord = save;
					annotator.showLeft("Relations");
					repaint();
					return;
				} else {
					unmarkAll();
					repaint();
				}
			} else {
				unmarkAll();
				repaint();
			}
		}

		/** Invoked when the mouse cursor has been moved onto a component
		 * 	but no buttons have been pushed. */
		public void mouseMoved(MouseEvent event) {
			position.setLocation(event.getPoint());
			try {
				at.inverseTransform(position, position);
			} catch (NoninvertibleTransformException e) { }
			// highlight word
			int x = (int)position.getX();
			int y = (int)position.getY();
			int wordLit = highlitWord;
			int labelLit = highlitLabel;
			highlitWord = -1;
			highlitLabel = -1;
			if (length == 0)
				return;
			int yTag = getHeight() - padding;
			int yWord = yTag - tags.fontHeight - lineSpace;
			if (yWord - words.fontHeight <= y && y <= yWord) {
				// on a word
				for (int i = 0; i < length; i++) {
					if (wordX[i] <= x && x <= wordX[i] + words.widths[i] &&
							markWord != i) { // marked at previous Click
						highlitWord = i;
						break;
					}
				}
			} else {
				for (int i = 0; i < length; i++) {
					if (sentence.parents[i] != 0) {
						bounds.setBounds(depX[i] - 3,
								arrY - arrH[i] - deps.fontAscent - 1,
								depW[i] + 5, deps.fontAscent + deps.fontDescent);
						if (bounds.contains(x, y)) {
							highlitLabel = i;
							break;
						}
					}
				}
			}
			if (highlitWord != wordLit || highlitLabel != labelLit)
				repaint();
		}

		AnnotateCanvas canvas;
		
		Interactor(AnnotateCanvas canvas) {
			this.canvas = canvas;
		}
	}

	protected void markWord(int selected) {
		markWord = selected;
		setCursor(crosshairCursor);
	}

	protected void unmarkWord() {
		markWord = -1;
		setCursor(Cursor.getDefaultCursor());
	}

	protected void unmarkAll() {
		markWord = -1;
		tagWord = -1;
		depWord = -1;
		highlitWord = -1;
		highlitLabel = -1;
		setCursor(Cursor.getDefaultCursor());
	}

	public void update() {
		if (length == 0)
			return;
		// compute link heights
		int linkHeight = deps.fontHeight + 3;
		for (int i = 0; i < length; i++) {
			arrH[i] = 0;
			arrInH[i] = 0;
		}
		for (int span = 1; span < length; span++) {
			for (int i = 0; i < length; i++) {
				// compute height of each link
				if (sentence.parents[i] == span) {
					// link from i -> i+span. Must be one higher than any inner
					// link
					for (int inner = i + 1; inner < i + span; inner++) {
						arrH[i] = Math.max(arrH[i], Math.max(arrH[inner],
								arrInH[inner]));
					}
					arrH[i] += linkHeight;
					arrInH[i + span] = Math.max(arrInH[i + span], arrH[i]);
				} else if (sentence.parents[i] == -span) {
					// link from i -> i-span. Must be one higher than any inner
					// link
					for (int inner = i - 1; inner > i - span; inner--) {
						arrH[i] = Math.max(arrH[i], Math.max(arrH[inner],
								arrInH[inner]));
					}
					arrH[i] += linkHeight;
					arrInH[i - span] = Math.max(arrInH[i - span], arrH[i]);
				}
			}
		}
		// compute height of root links
		for (int root = 0; root < length; root++) {
			if (sentence.parents[root] == Integer.MAX_VALUE) {
				// look for links crossing over root
				for (int i = 0; i < root; i++) {
					if (sentence.parents[i] >= root - i)
						arrH[root] = Math.max(arrH[root], arrH[i]);
				}
				for (int i = root; i < length; i++) {
					if (sentence.parents[i] <= root - i)
						arrH[root] = Math.max(arrH[root], arrH[i]);
				}
				arrH[root] += linkHeight;
			}
		}

		// compute number of arrows ending in each node
		for (int i = 0; i < length; i++)
			inlinksLeft[i] = inlinksRight[i] = 1;
		for (int i = 0; i < length; i++) {
			if (sentence.parents[i] > 0
					&& sentence.parents[i] != Integer.MAX_VALUE) {
				// horizontal links from the left
				int to = i + sentence.parents[i];
				inlinksLeft[to]++;
			}
		}
		for (int i = length - 1; i >= 0; i--) {
			// horizontal links from the right
			if (sentence.parents[i] < 0) {
				int to = i + sentence.parents[i];
				inlinksRight[to]++;
			}
		}

		// compute word positions
		int xi = padding; // to center add: + (width - wi) / 2;
		for (int i = 0; i < length; i++) {
			int padLeft = 0;
			int padRight = 0;
			if (sentence.parents[i] == 1) {
				// add padding for deps label to right
				int arrowLength = tokenWidth[i] / 2 + tokenWidth[i+1] / 2
						- inlinksLeft[i+1] * 4;
				padRight += Math.max(0, (deps.widths[i] - arrowLength) / 2);
			} else if (sentence.parents[i] == -1) {
				// add padding for deps label to left
				int arrowLength = tokenWidth[i-1] / 2 + tokenWidth[i] / 2
						- inlinksRight[i-1] * 4;
				padLeft += Math.max(0, (deps.widths[i] - arrowLength) / 2);
			}
			if (i > 0 && sentence.parents[i-1] == 1) {
				// add padding for deps label from left
				int arrowLength = tokenWidth[i-1] / 2 + tokenWidth[i] / 2
						- inlinksLeft[i] * 4;
				padLeft += Math.max(0, (deps.widths[i-1] - arrowLength) / 2);
			}
			if (i+1 < length && sentence.parents[i+1] == -1) {
				// add padding for deps label from right
				int arrowLength = tokenWidth[i] / 2 + tokenWidth[i+1] / 2
						- inlinksRight[i] * 4;
				padRight += Math.max(0, (deps.widths[i+1] - arrowLength) / 2);
			}
			wordX[i] = xi + padLeft + (tokenWidth[i] - words.widths[i]) / 2;
			tagX[i] = xi + padLeft + (tokenWidth[i] - tags.widths[i]) / 2;
			xi += padLeft + tokenWidth[i] + padRight + iSpace;
		}

		// compute arrows end
		for (int i = 0; i < length; i++)
			inlinksLeft[i] = inlinksRight[i] = 1;
		for (int i = 0; i < length; i++) {
			if (sentence.parents[i] > 0
					&& sentence.parents[i] != Integer.MAX_VALUE) {
				// horizontal links from the left
				int to = i + sentence.parents[i];
				arrE[i] = wordX[to] + words.widths[to] / 2 - inlinksLeft[to] * 4;
				inlinksLeft[to]++;
			}
		}
		for (int i = length - 1; i >= 0; i--) {
			// horizontal links from the right
			if (sentence.parents[i] < 0) {
				int to = i + sentence.parents[i];
				arrE[i] = wordX[to] + words.widths[to] / 2 + inlinksRight[to] * 4;
				inlinksRight[to]++;
			}
		}

		// compute preferred size
		textWidth = xi + padding;
		textHeight = 0; // max word height
		for (int i = 0; i < length; i++)
			if (arrH[i] > textHeight)
				textHeight = arrH[i];
		textHeight += linkHeight + words.fontHeight + tags.fontHeight + 8;
		revalidate();	// notify parent scroll pane
		repaint();
	}

	public void update2() {
		if (length == 0)
			return;
		// compute link heights
		int linkHeight = deps.fontHeight + 3;
		int root = -1;
		for (int i = 0; i < length; i++) {
			arrH2[i] = 0;
			arrInH2[i] = 0;
			if (sentence2.parents[i] == Integer.MAX_VALUE
					&& root == -1) // sometimes there is a second one
				root = i;
		}
		for (int span = 1; span < length; span++) {
			for (int i = 0; i < length; i++) {
				// compute height of each link
				if (sentence2.parents[i] == span) {
					// link from i -> i+span. Must be one higher than any inner
					// link
					for (int inner = i + 1; inner < i + span; inner++) {
						arrH2[i] = Math.max(arrH2[i], Math.max(arrH2[inner],
								arrInH2[inner]));
					}
					arrH2[i] += linkHeight;
					arrInH2[i + span] = Math.max(arrInH2[i + span], arrH2[i]);
				} else if (sentence2.parents[i] == -span) {
					// link from i -> i-span. Must be one higher than any inner
					// link
					for (int inner = i; inner > i - span; inner--) {
						arrH2[i] = Math.max(arrH2[i], Math.max(arrH2[inner],
								arrInH2[inner]));
					}
					arrH2[i] += linkHeight;
					arrInH2[i - span] = Math.max(arrInH2[i - span], arrH2[i]);
				}
			}
		}
		if (root != -1) {
			// look for links crossing over root
			for (int i = 0; i < root; i++) {
				if (sentence.parents[i] >= root - i)
					arrH[root] = Math.max(arrH[root], arrH[i]);
			}
			for (int i = root; i < length; i++) {
				if (sentence.parents[i] <= root - i)
					arrH[root] = Math.max(arrH[root], arrH[i]);
			}
			arrH[root] += linkHeight;
		}

		// compute arrows positions
		for (int i = 0; i < length; i++)
			inlinksLeft2[i] = inlinksRight2[i] = 1;
		for (int i = 0; i < length; i++) {
			if (sentence2.parents[i] > 0
					&& sentence2.parents[i] != Integer.MAX_VALUE) {
				// horizontal links from the left
				int to = i + sentence2.parents[i];
				arrE2[i] = (wordX[to] + words.widths[to] / 2) - inlinksLeft2[to] * 4;
				inlinksLeft2[to]++;
			}
		}
		for (int i = length - 1; i >= 0; i--) {
			// horizontal links from the right
			if (sentence2.parents[i] < 0) {
				int to = i + sentence2.parents[i];
				arrE2[i] = (wordX[to] + words.widths[to] / 2) + inlinksRight2[to] * 4;
				inlinksRight2[to]++;
			}
		}
	}

	protected void drawArrow(Graphics g, int x, int y, int height, int end) {
		// draw vertical segment up
		g.drawLine(x, y, x, y - height);
		// draw horizontal segment
		g.drawLine(x, y - height, end, y - height);
		// draw vertical segment down
		g.drawLine(end, y - height, end, y);
		// draw arrow point
		int ax[] = { x - 2, x + 2, x };
		int ay[] = { y - 5, y - 5, y + 1 };
		g.fillPolygon(new Polygon(ax, ay, 3));
	}

	protected void drawArrowElbow(Graphics g, int x, int y, int height,
			int width) {
		g.drawLine(x, y, x, y - height); // vertical segment up
		g.drawLine(x, y - height, x + width + 6, y - height); // horizontal
		// segment
		// draw arrow point
		int ax[] = { x - 2, x + 2, x };
		int ay[] = { y - 5, y - 5, y + 1 };
		g.fillPolygon(new Polygon(ax, ay, 3));
	}

	/**
	 * Offset of arrows in compared sentence
	 */
	static final int diff = 2;
	/**
	 * Padding to border
	 */
	static final int padding = 6;

	/**
	 * Vertical spacing
	 */
	static final int lineSpace = 4;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (length == 0)
			return;
		// save current transform in editing view
		editing.transform = at;

		// compute preferred size
		Dimension size = getSize();
		int height = size.height;
		int width = textWidth + (int)at.getTranslateX() + 2 * padding;
		setPreferredSize(new Dimension(width, height));

		// compose our transform with the current
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		AffineTransform saveXform = g2.getTransform();
		g2.transform(at);

		Font saveFont = g.getFont();
		Color saveColor = g.getColor();
		int yTags = height - padding;
		int yWords = yTags - tags.fontHeight - lineSpace;
		// draw words and tags
		g.setFont(words.font);
		for (int i = 0; i < length; i++) {
			// draw word
			if (markWord == i) {
				// color marked word in blue
				g.setColor(selWordColor);
				g.fill3DRect(wordX[i] - 3, yWords - words.fontAscent, words.widths[i] + 5,
						words.fontHeight, true);
				g.setColor(saveColor);
			}
			// signal words with no link
			if (sentence.parents[i] == 0 && i != markWord) {
				g.setColor(noDepColor);
				g.fillRect(wordX[i] - 2, yWords - words.fontAscent,
						words.widths[i] + 3, words.fontHeight);
				g.setColor(saveColor);
			}
			if (highlitWord == i) {
				g.setColor(highlitWordColor);
				g.setFont(words.fontHighlight);
				int w = getFontMetrics(words.fontHighlight).stringWidth(words.words[i]);
				g.drawString(words.words[i], wordX[i] - (w - words.widths[i])/2, yWords);
				g.setFont(saveFont);
				g.setColor(saveColor);
			} else {
				g.setFont(words.font);
				g.drawString(words.words[i], wordX[i], yWords);
				g.setFont(saveFont);
			}
			// draw POS tag
			if (tags.widths[i] != 0) {
				if (tagWord == i) {
					// color marked tag in pink
					g.setColor(selTagColor);
					g.fill3DRect(tagX[i] - 3, yTags - tags.fontAscent, tags.widths[i] + 5,
							tags.fontHeight, true);
					g.setColor(saveColor);
				}
				g.setFont(tags.font);
				g.drawString(tags.words[i], tagX[i], yTags);
				g.setFont(saveFont);
			}
		}

		// draw link arrows
		arrY = height - (words.fontHeight + tags.fontHeight + 9);
		g.setFont(deps.font);
		for (int i = 0; i < length; i++) {
			if (sentence.parents[i] != 0) {
				int arrX = wordX[i] + words.widths[i] / 2; // start x position
				// of arrow
				String s = deps.words[i];
				depW[i] = deps.widths[i]; // width of deps word
				if (sentence.parents[i] == Integer.MAX_VALUE) {
					// link to root
					drawArrowElbow(g, arrX, arrY, arrH[i], depW[i]);
					depX[i] = arrX + 3;
					if (depWord == i) {
						g.setColor(selDepColor);
						g.fill3DRect(depX[i] - 3, arrY - arrH[i] - deps.fontAscent - 1,
								depW[i] + 5, deps.fontAscent + deps.fontDescent, true);
						g.setColor(saveColor);
						g.drawString(s, arrX + 3, arrY - arrH[i] - 1);
					} else if (highlitLabel == i) {
						g.setColor(highlitLabelColor);
						g.drawString(s, depX[i], arrY - arrH[i] - 1);
						g.setColor(saveColor);
					} else
						g.drawString(s, arrX + 3, arrY - arrH[i] - 1);
				} else {
					drawArrow(g, arrX, arrY, arrH[i], arrE[i]);
					int linkWidth = Math.abs(arrX - arrE[i]);
					int depWi = depW[i];
					g.setFont(deps.font);
					int fontAscent = getFontMetrics(deps.font).getAscent();
					int fontDescent = getFontMetrics(deps.font).getDescent();

					// draw dependency label
					if (arrE[i] > arrX) {
						// link to the right
						if (depWi > linkWidth) {
							// doesn't fit
							depX[i] = arrX - (depWi - linkWidth) / 2;
						} else {
							depX[i] = arrX + (linkWidth - depWi) / 2;
						}
					} else {
						// link to the left
						if (depWi > linkWidth)
							depX[i] = arrE[i] - (depWi - linkWidth) / 2;
						else
							depX[i] = arrE[i] + (linkWidth - depWi) / 2;
					}
					if (depWord == i) {
						// set red background if selected
						g.setColor(selDepColor);
						g.fill3DRect(depX[i] - 3, arrY - arrH[i] - fontAscent - 1,
								depW[i] + 5, fontAscent + fontDescent, true);
						g.setColor(saveColor);
						g.drawString(s, depX[i], arrY - arrH[i] - 1);
					} else if (highlitLabel == i) {
						g.setColor(highlitLabelColor);
						g.drawString(s, depX[i], arrY - arrH[i] - 1);
						g.setColor(saveColor);
					} else
						g.drawString(s, depX[i], arrY - arrH[i] - 1);
				}
			}
			// draw different dependencies from alternative sentence tree
			if (sentence2 != null && sentence2.parents[i] != sentence.parents[i]) {
				g.setColor(altArrowColor);
				int arrX = wordX[i] + words.widths[i] / 2; // start x position of arrow
				int depWi = deps.widths[i]; // width of deps word
				if (sentence2.parents[i] == Integer.MAX_VALUE) {
					// link to root
					drawArrowElbow(g, arrX + diff, arrY, arrH2[i] - diff, depWi);
				} else {
					drawArrow(g, arrX + diff , arrY, arrH2[i] - diff, arrE2[i] + diff);
				}
				g.setColor(saveColor);
			}
		}
		g.setColor(saveColor);
		g.setFont(saveFont);
		// restore previous transform
		g2.setTransform(saveXform);
	}

	public BufferedImage getImage() {
		Dimension componentSize = getPreferredSize();
		setSize(componentSize); // Make sure these are the same
		BufferedImage img = new BufferedImage(componentSize.width,
				componentSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D grap = img.createGraphics();
		grap.fillRect(0, 0, img.getWidth(), img.getHeight());
		paint(grap);
		return img;
	}

	int iSpace; ///< space between words
	int markWord = -1; ///< index of marked word. -1 means no selection
	int tagWord = -1; ///< index of marked POS tag. -1 means no selection
	int depWord = -1; ///< index of selected dependency
	int highlitWord = -1;	///< index of word to be highlitWord
	int highlitLabel = -1;	///< index of dependency label to be highlitWord

	int length = 0; ///< sentence length in words

	SentenceView editing;	///< the view of the sentence being edited
	Sentence	sentence; ///< the model being viewed and manipulated (MVC)
	SentenceMetrics words;
	SentenceMetrics tags;
	SentenceMetrics lemmas;
	SentenceMetrics deps;

	int[] tokenWidth; ///< width of tokens
	int[] wordX; ///< words x position
	int[] tagX; ///< tags x position
	int	arrY; ///< arrow start y position
	int[] arrE; ///< arrows ends x position
	int[] arrH; ///< heights of outgoing arrows
	int[] arrInH; ///< heights of incoming arrows
	int[] depX; ///< dep words x position
	int[] depW; ///< dep words width
	int[] inlinksLeft; ///< incoming links from the left
	int[] inlinksRight; ///< incoming links from the right

	// for sentence to compare with
	Sentence	sentence2; ///< sentence to compare with
	SentenceMetrics deps2;
	int   arrY2; ///< arrow start y position
	int[] arrE2; ///< arrows ends x position
	int[] arrH2; ///< heights of outgoing arrows
	int[] arrInH2; ///< heights of incoming arrows
	int[] depX2; ///< dep words x position
	int[] depW2; ///< dep words width
	int[] inlinksLeft2; ///< incoming links from the left
	int[] inlinksRight2; ///< incoming links from the right

	int textHeight;		///< full height of tree
	int textWidth;		///< full width of tree

	public DGA frame; ///< frame whose menubar and toolbar item need enabling

	/**
	 * For editing words.
	 */
	private WordEdit wordEdit = null;
	
	/**
	 * Used to edit a word.
	 * @author Attardi
	 *
	 */
	@SuppressWarnings("serial")
	class WordEdit extends JTextField {
		public AnnotateCanvas canvas;
		public int idx;
		
		public WordEdit(AnnotateCanvas canvas) {
			this.canvas = canvas;
			setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		/**
		 * Pops up a JTextField to allow editing the word at the given point.
		 * After editing an action TokenEdit is generated.
		 * @param canvas on which we are editing.
		 * @param p the position of the mouse on the word to edit.
		 */
		public void edit(AnnotateCanvas canvas, Point p, int idx) {
			this.idx = idx;
			// set suitable width
			SentenceMetrics metrics = canvas.words;
			String word = metrics.words[idx];
			this.setSize(metrics.widths[idx], metrics.fontHeight + 4);
			setFont(metrics.font);
			this.setText(word);
			setLocation(p);
			addFocusListener(new FocusListener() {
		    	  @Override
		    	  public void focusGained(FocusEvent ev) { }

		    	  @Override
		    	  public void focusLost(FocusEvent ev) {
		    		  WordEdit wordEdit = (WordEdit)ev.getSource();
		    		  String word = wordEdit.getText().trim();
		    		  AnnotateCanvas canvas = wordEdit.canvas;
		    		  TokenEdit te = new TokenEdit(wordEdit.idx, word);
		    		  canvas.undo.addEdit(te);
		    		  te.redo();
		    		  setVisible(false);
		    	  }
		      });
			setVisible(true);
			requestFocusInWindow();
		}
	}
	  
	Interactor ml = new Interactor(this);
	Annotator annotator;
	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
	}

	public AnnotateCanvas() {
		setBackground(Color.white);
	    wordEdit = new WordEdit(this);
	    add(wordEdit);
		addMouseListener(ml);
		addMouseMotionListener(ml);
		addMouseWheelListener(ml);
		addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyTyped(java.awt.event.KeyEvent e) {
				if (e.isControlDown()) {
					switch (e.getKeyChar() + '@') {
					case 'Z':
						undo();
						break;
					case 'Y':
						redo();
					}
				}
			}
			public void keyPressed(java.awt.event.KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					deleteAction();
				}
			}
		});
	}

	private void deleteAction() {
		if (depWord >= 0) {
			// Undo
			DeleteArcEdit se = new DeleteArcEdit(depWord, sentence.parents[depWord], sentence.deps[depWord]);
			undo.addEdit(se);
			se.redo();
			frame.enableRedo(undo.canRedo());
			depWord = -1;
		}
	}

	public void EnablePanning() {
		ml.setMode(Interactor.Mode.pan);
	}

	public void EnableZooming() {
		ml.setMode(Interactor.Mode.zoom);
	}

	protected void reset() {
		unmarkAll();
		frame.enableUndo(undo.canUndo());
		frame.enableRedo(undo.canRedo());
	}

	public void annotate(SentenceView editing) {
		this.editing = editing;
		this.undo = editing.undoManager;
		this.sentence = editing.sentence;
		this.at = editing.transform;
		reset();
		this.sentence2 = null;	// do not display it
		FontMetrics fontMetrics = getFontMetrics(wordFont);
		iSpace = fontMetrics.stringWidth(" ");
		words = new SentenceMetrics(fontMetrics, wordFontHighlight, sentence.forms);
		tags = new SentenceMetrics(getFontMetrics(tagFont), tagFont,
				editing.viewPOS ? sentence.tags : sentence.coarseTags);
		lemmas = new SentenceMetrics(fontMetrics, wordFontHighlight, sentence.lemmas);
		deps = new SentenceMetrics(getFontMetrics(depsFont), depsFont, sentence.deps);
		length = words.Length();
		// take widest word
		tokenWidth = new int[length];
		for (int i = 0; i < length; i++) {
			tokenWidth[i] = Math.max(words.widths[i], tags.widths[i]);
			tokenWidth[i] = Math.max(tokenWidth[i], lemmas.widths[i]);
		}
		wordX = new int[length];
		tagX = new int[length];
		arrH = new int[length];
		arrInH = new int[length];
		depX = new int[length];
		depW = new int[length];
		arrE = new int[length];
		inlinksLeft = new int[length];
		inlinksRight = new int[length];
		update();
	}

	public void clear() {
		this.editing = null;
		this.undo = null;
		this.sentence = null;
		at.setToIdentity();
		length = 0;
		frame.enableUndo(false);
		frame.enableRedo(false);
		repaint();
	}

	public void compareWith(Sentence sentence) {
		this.sentence2 = sentence;
		FontMetrics fontMetrics = getFontMetrics(tagFont);
		deps2 = new SentenceMetrics(fontMetrics, tagFont, sentence.deps);
		arrH2 = new int[length];
		arrInH2 = new int[length];
		depX2 = new int[length];
		depW2 = new int[length];
		arrE2 = new int[length];
		inlinksLeft2 = new int[length];
		inlinksRight2 = new int[length];
		update2();
		update();
	}

	public void setDependency(String dep) {
		if (depWord >= 0 && !dep.equals(sentence.deps[depWord])) {
			LabelEdit le = new LabelEdit(depWord, dep, sentence.deps[depWord]);
			undo.addEdit(le);
			le.redo();
		}
	}

	public void setTag(String tag) {
		if (tagWord >= 0 && !tag.equals(sentence.tags[tagWord])) {
			TagEdit le = new TagEdit(tagWord, tag, sentence.tags[tagWord]);
			undo.addEdit(le);
			le.redo();
			// advance to next word
			if (tagWord < length - 1)
				tagWord++;
		}
	}

	public void setToken(String tok) {
		if (markWord >= 0 && !tok.equals(sentence.forms[markWord])) {
			TokenEdit le = new TokenEdit(markWord, tok);
			undo.addEdit(le);
			le.redo();
			// advance to next word
			if (markWord < length - 1)
				markWord++;
		}
	}

	public void viewPOS(boolean coarse) {
		if (editing != null) {
			editing.viewPOS = coarse;
			annotate(editing);
		}
	}

	private static final long serialVersionUID = -1L;
}
