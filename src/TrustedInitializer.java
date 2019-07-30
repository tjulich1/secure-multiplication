import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.PrintWriter;

/**
*
*/
public class TrustedInitializer
{

  private int prime = 17321;
  private int u;
  private int v;
  private int w;
  private int aliceU;
  private int aliceV;
  private int aliceW;
  private int bobU;
  private int bobV;
  private int bobW;

  private Socket aliceSocket;
  private Socket bobSocket;
  private ServerSocket tiSocket;

  private PrintWriter aliceOut;
  private PrintWriter bobOut;

  /**
  * Constructor.
  */
  public TrustedInitializer()
  {

  }

  /**
  *
  */
  public void generateTriple()
  {
    u = (int) (Math.random() * prime);
    v = (int) (Math.random() * prime);
    w = Math.floorMod(u * v, prime);
  }

  /**
  *
  */
  public void generateShares()
  {
    aliceU = (int) (Math.random() * prime);
    aliceV = (int) (Math.random() * prime);
    aliceW = (int) (Math.random() * prime);

    bobU = Math.floorMod(u - aliceU, prime);
    bobV = Math.floorMod(v - aliceV, prime);
    bobW = Math.floorMod(w - aliceW, prime);
  }

  /**
  *
  */
  public void sendShares()
  {
    aliceOut.println(Integer.toString(aliceU));
    aliceOut.println(Integer.toString(aliceV));
    aliceOut.println(Integer.toString(aliceW));

    bobOut.println(Integer.toString(bobU));
    bobOut.println(Integer.toString(bobV));
    bobOut.println(Integer.toString(bobW));
  }

  /**
  *
  */
  public void start(final int port)
  {
    try
    {
      tiSocket = new ServerSocket(port);
      aliceSocket = tiSocket.accept();
      bobSocket = tiSocket.accept();

      aliceOut = new PrintWriter(aliceSocket.getOutputStream(), true);
      bobOut = new PrintWriter(bobSocket.getOutputStream(), true);
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
      tiSocket.close();
      aliceSocket.close();
      bobSocket.close();
      aliceOut.close();
      bobOut.close();
    } catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
  *
  */
  public static void main(String[] args)
  {
    TrustedInitializer ti = new TrustedInitializer();
    ti.generateTriple();
    ti.generateShares();
    ti.start(6668);
    ti.sendShares();
    ti.close();
  }

}
