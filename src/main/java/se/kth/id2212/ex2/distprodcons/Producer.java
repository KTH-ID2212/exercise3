package se.kth.id2212.ex2.distprodcons;

public class Producer implements Runnable
{
    RemoteBuffer buffer;
    int times;

    Producer(RemoteBuffer buffer, int times)
    {
        this.buffer = buffer;
        this.times = times;
    }

    public void run()
    {
        try
        {
            for (int i = 1; i <= times; i++)
            {
                buffer.put(i);
            }
            buffer.put(null); // Tells the consumer we are done.
        } catch (java.rmi.RemoteException re)
        {
            re.printStackTrace();
        }
    }
}
