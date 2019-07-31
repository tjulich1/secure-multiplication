import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;

/**
* @author Trent Julich
* @version 20 July 2019
*/
public class Alice
{
  private static final int PRIME = 17321;
  private static final int ALICE_NUMBER = 12;

  private int number;
  private ServerSocket serverSocket;
  private Socket bobSocket;
  private Socket aliceSocket;
  private Socket tiSocket;
  private PrintWriter out;
  private BufferedReader in;

  private int aliceU;
  private int aliceV;
  private int aliceW;

  private int aliceX;
  private int bobX;

  private int aliceY;

  private int d;
  private int e;

  private int productShare;

  /**
  * Constructor.
  *
  * @param number Alice's number.
  */
  public Alice(final int number)
  {
    this.number = number;
  }

  public void readSharesOfTriple()
  {
    try
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(tiSocket.getInputStream()));
      aliceU = Integer.parseInt(in.readLine());
      aliceV = Integer.parseInt(in.readLine());
      aliceW = Integer.parseInt(in.readLine());
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void createShares()
  {
    aliceX = (int) (Math.random() * PRIME);
    bobX = Math.floorMod(number - aliceX, PRIME);
    out.println(Integer.toString(bobX));
  }

  public void readBobShare()
  {
    try
    {
      aliceY = Integer.parseInt(in.readLine());
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void computeDandE()
  {
    final int d1 = Math.floorMod(aliceX - aliceU, PRIME);
    final int e1 = Math.floorMod(aliceY - aliceV, PRIME);
    out.println(Integer.toString(d1));
    out.println(Integer.toString(e1));
    try
    {
      final int d0 = Integer.parseInt(in.readLine());
      final int e0 = Integer.parseInt(in.readLine());
      d = Math.floorMod(d0 + d1, PRIME);
      e = Math.floorMod(e0 + e1, PRIME);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void computeFinalShare()
  {
    productShare = Math.floorMod(aliceW + d*aliceV + aliceU*e + d*e, PRIME);
    out.println(Integer.toString(productShare));
  }

  public void readFinalShare()
  {
    try
    {
      final int otherShare = Integer.parseInt(in.readLine());
      final int finalProduct = Math.floorMod(productShare + otherShare, PRIME);
      System.out.println("Final product = " + finalProduct);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /////////////////////
  // Network methods //
  /////////////////////

  public void open(final int port) throws IOException
  {
    serverSocket = new ServerSocket(port);
  }

  public void close() throws IOException
  {
    serverSocket.close();
    bobSocket.close();
    aliceSocket.close();
    in.close();
    out.close();
  }

  public void connectToTI(final String IP, final int port) throws IOException
  {
    tiSocket = new Socket(IP, port);
  }

  public void connectToBob(final String IP, final int port) throws IOException
  {
    aliceSocket = new Socket(IP, port);
    in = new BufferedReader(new InputStreamReader(aliceSocket.getInputStream()));
  }

  public void closeTI() throws IOException
  {
    tiSocket.close();
  }

  public void waitForBob()
  {
    Thread thread = new Thread(new WaitThread());
    thread.start();
  }

  public static void main(String[] args) throws IOException
  {
    final Alice alice = new Alice(ALICE_NUMBER);
    try
    {
      alice.connectToTI("127.0.0.1", 6668);
      alice.readSharesOfTriple();
      alice.closeTI();
      alice.open(6666);
      alice.waitForBob();
      alice.connectToBob("127.0.0.1", 6667);
      try
      {
        Thread.sleep(2000);
      }
      catch (final InterruptedException e)
      {
        e.printStackTrace();
      }
      alice.createShares();
      alice.readBobShare();
      alice.computeDandE();
      alice.computeFinalShare();
      alice.readFinalShare();
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      alice.close();
    }
  }

  private class WaitThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        bobSocket = serverSocket.accept();
        out = new PrintWriter(bobSocket.getOutputStream(), true);
      } catch (final IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
