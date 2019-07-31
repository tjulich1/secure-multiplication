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
* @version 30 July 2019
*/
public class Bob
{
  private static final int PRIME = 17321;
  private static final int BOB_NUMBER = 10;

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

  /**
  * Constructor.
  *
  * @param number Bob's number.
  */
  public Bob(final int number)
  {
    this.number = number;
  }

  public void readSharesOfTriple() throws IOException
  {
      BufferedReader in = new BufferedReader(new InputStreamReader(tiSocket.getInputStream()));
      bobU = Integer.parseInt(in.readLine());
      bobV = Integer.parseInt(in.readLine());
      bobW = Integer.parseInt(in.readLine());
  }

  public void createShares()
  {
    bobY = (int) (Math.random() * PRIME);
    aliceY = Math.floorMod(number - bobY, PRIME);
    System.out.println("BOB: Alice share of y = " + aliceY);
    out.println(Integer.toString(aliceY));
  }

  public void readAliceShare() throws IOException
  {
    bobX = Integer.parseInt(in.readLine());
  }

  public void computeDandE() throws IOException
  {
    final int d0 = Math.floorMod(bobX - bobU, PRIME);
    final int e0 = Math.floorMod(bobY - bobV, PRIME);
    out.println(Integer.toString(d0));
    out.println(Integer.toString(e0));

    final int d1 = Integer.parseInt(in.readLine());
    final int e1 = Integer.parseInt(in.readLine());
    d = Math.floorMod(d0 + d1, PRIME);
    e = Math.floorMod(e0 + e1, PRIME);
  }

  public void computeFinalShare()
  {
    productShare = Math.floorMod(bobW + d*bobV + bobU*e, PRIME);
    out.println(Integer.toString(productShare));
  }

  public void readFinalShare() throws IOException
  {
    final int otherShare = Integer.parseInt(in.readLine());
    final int finalProduct = Math.floorMod(otherShare + productShare, PRIME);
    System.out.println("Final product: " + finalProduct);
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
    aliceSocket.close();
    bobSocket.close();
    in.close();
    out.close();
  }

  public void connectToTI(final String IP, final int port) throws IOException
  {
    tiSocket = new Socket(IP, port);
  }

  public void connectToAlice(final String IP, final int port) throws IOException
  {
    bobSocket = new Socket(IP, port);
    in = new BufferedReader(new InputStreamReader(bobSocket.getInputStream()));
  }

  public void closeTI() throws IOException
  {
    tiSocket.close();
  }

  public void waitForAlice()
  {
    Thread thread = new Thread(new WaitThread());
    thread.start();
  }

  public static void main(String[] args) throws IOException
  {
    final Bob bob = new Bob(BOB_NUMBER);
    try
    {
      bob.connectToTI("127.0.0.1", 6668);
      bob.readSharesOfTriple();
      bob.closeTI();
      bob.open(6667);
      bob.waitForAlice();
      bob.connectToAlice("127.0.0.1", 6666);
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
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      bob.close();
    }
  }

  private class WaitThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        aliceSocket = serverSocket.accept();
        out = new PrintWriter(aliceSocket.getOutputStream(), true);
      } catch (final IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
