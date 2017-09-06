import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.*;

public class Snake extends JFrame {

	private static final int SIZE = 64, TILE_SIZE = 14;
	private static final Color DARK_GREEN = Color.GREEN.darker();
	private static final Color DARK_CYAN = Color.CYAN.darker();

	enum Direction {

		LEFT(-1, 0),
		UP(0, -1),
		RIGHT(1, 0),
		DOWN(0, 1);

		public int xoff, yoff;

		Direction(int x, int y) {
			xoff = x;
			yoff = y;
		}

		static Direction parse(int keycode, Direction previous) {
			return (keycode<37 || keycode > 40)?previous:Direction.values()[keycode-37];
		}

	}

	private LinkedList<Point> list = new LinkedList<>();
	private Direction current = Direction.DOWN, toChange = Direction.DOWN;
	private Random random = new Random();
	private boolean alive = true;
	private boolean mesh = true;
	private boolean colors = true;
	private Point fruit = new Point();
	private int type = 1; //fruit type: 1:3, 3:9, 6:18 fruits
	private Font font;

	private JPanel panel = new JPanel() {
		protected void paintComponent(java.awt.Graphics arg0) {
			arg0.setColor(colors ? Color.WHITE : Color.BLACK);
			arg0.fillRect(0, 0, panel.getWidth(), panel.getHeight());

			if(mesh) {
				arg0.setColor(colors?Color.LIGHT_GRAY:Color.DARK_GRAY);
				for(int i = 0; i < SIZE; i++) {
					arg0.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, SIZE * TILE_SIZE);
					arg0.drawLine(0, i * TILE_SIZE, SIZE * TILE_SIZE, i * TILE_SIZE);
				}
			}

			arg0.setColor(type==1?Color.RED:type==2?Color.ORANGE:type==3?Color.BLUE:Color.PINK);
			arg0.fillOval(fruit.x* TILE_SIZE, fruit.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

			for(Point point : list) {
				arg0.setColor(list.indexOf(point)==0?(colors?Color.CYAN:DARK_CYAN):(colors?Color.GREEN:DARK_GREEN));
				arg0.fillRect(point.x* TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
			}

			arg0.setColor(colors?Color.BLACK:Color.LIGHT_GRAY);
			Font previous = arg0.getFont();
			arg0.setFont(font);
			arg0.drawString("Score: " + Integer.toString((list.size()-5)*5), 4, 24);
			arg0.setFont(previous);
			arg0.drawString("Press F2 to enable/disable mesh", 4, 40);
			arg0.drawString("Press F3 to change colors", 4, 54);
			if(!alive) arg0.drawString("You died! Press F1 to start again!", 4, 68);
		}
	};

	private Snake() {
		super("Snake");
		font = new Font("Arial", Font.BOLD, 24);
		panel.setFont(new Font("Arial", Font.BOLD, 12));
		panel.setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));

		addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent arg0) {
				toChange = Direction.parse(arg0.getKeyCode(), toChange);
				if(arg0.getKeyCode() == KeyEvent.VK_F1 && !alive) start();
				if(arg0.getKeyCode() == KeyEvent.VK_F2) {
					mesh = !mesh;
					panel.repaint();
				}
				if(arg0.getKeyCode() == KeyEvent.VK_F3) {
					colors = !colors;
					panel.repaint();
				}
			}
		});

		setContentPane(panel);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
		start();
	}


	public void start() {
		new Thread(() -> {
			list.clear();
			for(int i=0;i<5;i++) list.add(new Point(SIZE /2, SIZE /2));
			generateFruit();

			int speed = 0;
			alive = true;
			while(alive) {

				if(Math.abs(toChange.xoff - current.xoff)==1 && Math.abs(toChange.yoff - current.yoff)==1)
					current = toChange;

				Point temp = new Point((int)list.get(0).getX()+current.xoff, (int)list.get(0).getY()+current.yoff);
				for(Point point : list) if(point.equals(temp)) alive = false;

				if(alive && temp.x >= 0 && temp.x < SIZE && temp.y >= 0 && temp.y < SIZE) {
					list.addFirst(list.pollLast());
					list.get(0).setLocation(temp.x, temp.y);

					if(list.get(0).equals(fruit)) {
						if(type !=2)
							for(int i=0;i<type*3;i++) list.addLast(new Point(list.getLast().getLocation()));
						else speed+=2;

						generateFruit();
					}

					try {
						Thread.sleep(Math.max(1,50-speed));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else
					alive = false;

				panel.repaint();
			}
		}).start();
	}

	private void generateFruit() {
		boolean wrong;
		do {
			wrong = false;
			fruit.setLocation(random.nextInt(SIZE -2)+1, random.nextInt(SIZE -2)+1);

			double x = random.nextDouble();
			type = (x < 0.82 ? 1 : (x < 0.89 ? 2 : (x < 0.99 ? 3 : 6)));

			for(Point point : list) if(point.equals(fruit)) wrong = true;
		} while (wrong);
	}

	public static void main(String[] args) {
		new Snake();
	}

}