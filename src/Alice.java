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

  private int prime = 17321;

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
    aliceX = (int) (Math.random() * prime);
    bobX = Math.floorMod(number - aliceX, prime);
    System.out.println("ALICE: bob share of x = " + bobX);
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
    System.out.println("ALICE: alice share of y = " + aliceY);
  }

  public void computeDandE()
  {
    final int d1 = Math.floorMod(aliceX - aliceU, prime);
    final int e1 = Math.floorMod(aliceY - aliceV, prime);
    out.println(Integer.toString(d1));
    out.println(Integer.toString(e1));
    try
    {
      final int d0 = Integer.parseInt(in.readLine());
      final int e0 = Integer.parseInt(in.readLine());
      d = Math.floorMod(d0 + d1, prime);
      e = Math.floorMod(e0 + e1, prime);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void computeFinalShare()
  {
    productShare = Math.floorMod(aliceW + d*aliceV + aliceU*e + d*e, prime);
    out.println(Integer.toString(productShare));
  }

  public void readFinalShare()
  {
    try
    {
      final int otherShare = Integer.parseInt(in.readLine());
      final int finalProduct = Math.floorMod(productShare + otherShare, prime);
      System.out.println("Final product = " + finalProduct);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /////////////////////
  // Network methods //
  /////////////////////

  /**
  *
  */
  public void open(final int port)
  {
    try
    {
      serverSocket = new ServerSocket(port);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
  *
  */
  public void close()
  {
    try
    {
      serverSocket.close();
      bobSocket.close();
      aliceSocket.close();
      in.close();
      out.close();
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
  *
  */
  public void connectToTI()
  {
    try
    {
      tiSocket = new Socket("127.0.0.1", 6668);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void connectToBob()
  {
    try
    {
      aliceSocket = new Socket("127.0.0.1", 6667);
      in = new BufferedReader(new InputStreamReader(aliceSocket.getInputStream()));
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
  *
  */
  public void closeTI()
  {
    try
    {
      tiSocket.close();
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void waitForBob()
  {
    Thread thread = new Thread(new WaitThread());
    thread.start();
  }

  /**
  *
  */
  public static void main(String[] args)
  {
    final Alice alice = new Alice(3);
    alice.connectToTI();
    alice.readSharesOfTriple();
    alice.closeTI();
    alice.open(6666);
    alice.waitForBob();
    alice.connectToBob();

    try
    {
      Thread.sleep(2000);
    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    }

    alice.createShares();
    alice.readBobShare();
    alice.computeDandE();
    alice.computeFinalShare();
    alice.readFinalShare();

    alice.close();
  }

  private class WaitThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        bobSocket = serverSocket.accept();
        System.out.println("ALICE: Connected to bob");
        out = new PrintWriter(bobSocket.getOutputStream(), true);
      } catch (final IOException e)
      {
        e.printStackTrace();
      }
    }
  }

}
