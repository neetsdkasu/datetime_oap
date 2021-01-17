import java.util.Calendar;
import java.util.Date;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.*;

public final class DateTimeMIDlet extends MIDlet implements CommandListener
{

    DateTimeCanvas canvas = null;
    Command exitCommand = null;
    Thread thread = null;

    public DateTimeMIDlet()
    {

    }

    protected void startApp() throws MIDletStateChangeException
    {
        if (canvas == null)
        {
            canvas = new DateTimeCanvas();
            canvas.addCommand(exitCommand = new Command("EXIT", Command.EXIT, 1));
            canvas.setCommandListener(this);
            Display.getDisplay(this).setCurrent(canvas);
            (thread = new Thread(canvas)).start();
        }
    }

    protected void pauseApp()
    {

    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        if (thread != null)
        {
            thread.interrupt();
        }
    }

    public void commandAction(Command c, Displayable d)
    {
        if (c != null && c == exitCommand)
        {
            if (thread != null)
            {
                thread.interrupt();
            }
            notifyDestroyed();
        }
    }
}

class DateTimeCanvas extends GameCanvas implements Runnable
{
    final String[] KANJI;
    final String[] DAYS = new String[31];
    final String[] NUMBERS = new String[100];
    final int[] LAST = new int[]{
        31, // 1
        28, // 2
        31, // 3
        30, // 4
        31, // 5
        30, // 6
        31, // 7
        31, // 8
        30, // 9
        31, // 10
        30, // 11
        31  // 12
    };

    DateTimeCanvas()
    {
        super(false);
        String[] kanji;
        try
        {
            char[] chars = (new String(new byte[]{
                -26, -105, -91,
                -26, -100, -120,
                -25, -127, -85,
                -26, -80, -76,
                -26, -100, -88,
                -23, -121, -111,
                -27, -100, -97,
                -27, -71, -76
            }, "UTF-8")).toCharArray();
            kanji = new String[8];
            for (int i = 0; i < 8; i++)
            {
                kanji[i] = String.valueOf(chars[i]);
            }
        }
        catch (Exception e)
        {
            kanji = new String[]{
                "SUN",
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT",
                "YEA"
            };
        }
        KANJI = kanji;
        for (int i = 0; i < 100; i++)
        {
            if (i < 10)
            {
                NUMBERS[i] = "0" + i;
                DAYS[i] = String.valueOf(i+1);
            }
            else
            {
                NUMBERS[i] = String.valueOf(i);
                if (i <= 31)
                {
                    DAYS[i-1] = NUMBERS[i];
                }
            }
        }
    }

    int update = -1;
    Font font = null;
    Graphics g = null;

    void draw()
    {
        if (g == null)
        {
            g = getGraphics();
        }
        if (font == null)
        {
            font = Font.getFont(
                Font.FACE_SYSTEM,
                Font.STYLE_PLAIN,
                Font.SIZE_LARGE
            );
            g.setFont(font);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH); // 0-11
        int day = cal.get(Calendar.DAY_OF_MONTH); // 1-31
        int weekday = cal.get(Calendar.DAY_OF_WEEK); // 1-7
        int hour = cal.get(Calendar.HOUR_OF_DAY); // 0-23
        int minute = cal.get(Calendar.MINUTE); // 0-59
        int second = cal.get(Calendar.SECOND); // 0-59

        String sYear = NUMBERS[year / 100] + NUMBERS[year % 100];

        if (update != day)
        {
            g.setColor(0x000000);
            g.fillRect(0, 0, 240, 268);

            g.setColor(0xFFFFFF);
            g.drawString(
                sYear + KANJI[7] + " " + DAYS[month] + KANJI[1],
                120,
                0,
                Graphics.HCENTER|Graphics.TOP
            );

            g.setColor(0xFFFFFF);
            for (int i = 0; i < 7; i++)
            {
                g.drawString(
                    KANJI[i],
                    30 * (i+1),
                    font.getHeight()+2,
                    Graphics.HCENTER|Graphics.TOP
                );
            }

            int dLast = LAST[month];
            if (month == Calendar.FEBRUARY && (year & 3) == 0)
            {
                if (year % 400 == 0 || year % 100 != 0)
                {
                    dLast++;
                }
            }
            int w = ((weekday-1)-(day-1)%7+7)%7;
            int y = (font.getHeight()+2) * 2;
            g.setColor(0xFFFFFF);
            for (int d = 0; d < dLast; d++)
            {
                g.drawString(
                    DAYS[d],
                    30 * (w+1),
                    y,
                    Graphics.HCENTER|Graphics.TOP
                );

                if (d+1 == day)
                {
                    g.setColor(0x00FFFF);
                    g.drawRect(
                        30 * (w+1) - 15,
                        y-1,
                        29,
                        font.getHeight()
                    );
                    g.setColor(0xFFFFFF);
                }

                w++;
                if (w >= 7)
                {
                    w = 0;
                    y += font.getHeight()+2;
                }
            }

            g.setColor(0xFFFFFF);
            g.drawString(
                sYear + "-" + NUMBERS[month+1] + "-" + NUMBERS[day] + " (" + KANJI[weekday-1] + ")",
                120,
                240 - font.getHeight() - 2,
                Graphics.HCENTER|Graphics.BOTTOM
            );

        } else {
            g.setColor(0x000000);
            g.fillRect(
                0,
                240 - font.getHeight() - 1,
                240,
                font.getHeight() + 1
            );
        }

        g.setColor(0xFFFFFF);
        g.drawString(
            NUMBERS[hour] + ":" + NUMBERS[minute] + ":" + NUMBERS[second],
            120,
            240,
            Graphics.HCENTER|Graphics.BOTTOM
        );

        if (update != day)
        {
            update = day;
            flushGraphics();
        }
        else
        {
            flushGraphics(
                0,
                240 - font.getHeight() - 1,
                240,
                font.getHeight() + 1
            );
        }

    }

    public void run()
    {
        long next = System.currentTimeMillis() + 1000L;
        for (;;)
        {
            draw();
            try
            {
                long current = System.currentTimeMillis();
                long wait = Math.max(0L, next - current);
                Thread.sleep(wait);
            }
            catch (InterruptedException ie)
            {
                return;
            }
            next += 1000L;
        }
    }
}