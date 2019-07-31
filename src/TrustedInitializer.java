import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.PrintWriter;

/**
* Trusted Initializer is used to generate and distribute random shares of U,
* V, and W for Alice and Bob to use in their secure computations. After
* generation and distribution of random numbers, the trusted initializer is
* closed.
*/
public class TrustedInitializer
{

  private static final int PRIME = 17321;
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

  public void generateTriple()
  {
    u = (int) (Math.random() * PRIME);
    v = (int) (Math.random() * PRIME);
    w = Math.floorMod(u * v, PRIME);
  }

  public void generateShares()
  {
    aliceU = (int) (Math.random() * PRIME);
    aliceV = (int) (Math.random() * PRIME);
    aliceW = (int) (Math.random() * PRIME);

    bobU = Math.floorMod(u - aliceU, PRIME);
    bobV = Math.floorMod(v - aliceV, PRIME);
    bobW = Math.floorMod(w - aliceW, PRIME);
  }

  public void sendShares()
  {
    aliceOut.println(Integer.toString(aliceU));
    aliceOut.println(Integer.toString(aliceV));
    aliceOut.println(Integer.toString(aliceW));

    bobOut.println(Integer.toString(bobU));
    bobOut.println(Integer.toString(bobV));
    bobOut.println(Integer.toString(bobW));
  }

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
