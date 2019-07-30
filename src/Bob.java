import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.lang.InterruptedException;

/**
* @author Trent Julich
* @version 20 July 2019
*/
public class Bob
{

  private int number;
  private ServerSocket serverSocket;
  private Socket aliceSocket;
  private Socket bobSocket;
  private Socket tiSocket;
  private PrintWriter out;
  private BufferedReader in;

  private int bobU;
  private int bobV;
  private int bobW;

  private int aliceY;
  private int bobY;

  private int bobX;

  private int d;
  private int e;

  private int productShare;

  private int prime = 17321;

  /**
  * Constructor.
  *
  * @param number Bob's number.
  */
  public Bob(final int number)
  {
    this.number = number;
  }

  public void readSharesOfTriple()
  {
    try
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(tiSocket.getInputStream()));
      bobU = Integer.parseInt(in.readLine());
      bobV = Integer.parseInt(in.readLine());
      bobW = Integer.parseInt(in.readLine());
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void createShares()
  {
    bobY = (int) (Math.random() * prime);
    aliceY = Math.floorMod(number - bobY, prime);
    System.out.println("BOB: Alice share of y = " + aliceY);
    out.println(Integer.toString(aliceY));
  }

  public void readAliceShare()
  {
    try
    {
      bobX = Integer.parseInt(in.readLine());
      System.out.println("BOB: Bob share of x = " + bobX);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void computeDandE()
  {
    final int d0 = Math.floorMod(bobX - bobU, prime);
    final int e0 = Math.floorMod(bobY - bobV, prime);
    out.println(Integer.toString(d0));
    out.println(Integer.toString(e0));
    try
    {
      final int d1 = Integer.parseInt(in.readLine());
      final int e1 = Integer.parseInt(in.readLine());
      d = Math.floorMod(d0 + d1, prime);
      e = Math.floorMod(e0 + e1, prime);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  public void computeFinalShare()
  {
    productShare = Math.floorMod(bobW + d*bobV + bobU*e, prime);
    out.println(Integer.toString(productShare));
  }

  public void readFinalShare()
  {
    try
    {
      final int otherShare = Integer.parseInt(in.readLine());
      final int finalProduct = Math.floorMod(otherShare + productShare, prime);
      System.out.println("Final product: " + finalProduct);
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /////////////////////
  // Network methods //
  /////////////////////


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

  public void close()
  {
    try
    {
      serverSocket.close();
      aliceSocket.close();
      bobSocket.close();
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

  public void connectToAlice()
  {
    try
    {
      bobSocket = new Socket("127.0.0.1", 6666);
      in = new BufferedReader(new InputStreamReader(bobSocket.getInputStream()));
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

  public void waitForAlice()
  {
    Thread thread = new Thread(new WaitThread());
    thread.start();
  }

  /**
  *
  */
  public static void main(String[] args)
  {
    final Bob bob = new Bob(2);
    bob.connectToTI();
    bob.readSharesOfTriple();
    bob.closeTI();
    bob.open(6667);
    bob.waitForAlice();
    bob.connectToAlice();

    try
    {
      Thread.sleep(2000);
    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    }

    bob.createShares();
    bob.readAliceShare();
    bob.computeDandE();
    bob.computeFinalShare();
    bob.readFinalShare();

    bob.close();
  }

  private class WaitThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        aliceSocket = serverSocket.accept();
        System.out.println("BOB: Connected to alice");
        out = new PrintWriter(aliceSocket.getOutputStream(), true);
      } catch (final IOException e)
      {
        e.printStackTrace();
      }
    }
  }

}
