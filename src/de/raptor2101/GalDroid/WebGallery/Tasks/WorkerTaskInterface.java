package de.raptor2101.GalDroid.WebGallery.Tasks;

public interface WorkerTaskInterface {
  public enum Status {
    PENDING, RUNNING, SLEEPING, FINISHED, STOPPING
  }

  public Status getStatus();

  public void start();

  public void stop();

  public void cancel();
}
