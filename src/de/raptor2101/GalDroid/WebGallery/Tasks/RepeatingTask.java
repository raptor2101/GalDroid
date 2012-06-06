package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.lang.Thread.State;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class RepeatingTask<ParameterType, ProgressType, ResultType> implements WorkerTaskInterface {
  protected static final String CLASS_TAG = "RepeatingTask";
  private static final int MESSAGE_POST_RESULT = 1;
  private static final int MESSAGE_POST_PROGRESS = 2;
  private static final int MESSAGE_POST_ERROR = 3;
  private static final int MESSAGE_PRE_EXECUTION = 4;

  private class TaskMessage<MessageType> {
    public final ParameterType mParameter;
    public final MessageType mMessage;

    public TaskMessage(ParameterType parameter, MessageType message) {
      mMessage = message;
      mParameter = parameter;
    }
  }

  private class MessageHandler extends Handler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case MESSAGE_PRE_EXECUTION: {
        TaskMessage<ResultType> message = (TaskMessage<ResultType>) msg.obj;
        onPreExecute(message.mParameter);
        break;
      }
      case MESSAGE_POST_RESULT: {
        TaskMessage<ResultType> message = (TaskMessage<ResultType>) msg.obj;
        onPostExecute(message.mParameter, message.mMessage);
        break;
      }
      case MESSAGE_POST_PROGRESS: {
        TaskMessage<ProgressType> message = (TaskMessage<ProgressType>) msg.obj;
        onProgressUpdate(message.mParameter, message.mMessage);
        break;
      }
      case MESSAGE_POST_ERROR: {
        TaskMessage<Exception> message = (TaskMessage<Exception>) msg.obj;
        onExceptionThrown(message.mParameter, message.mMessage);
        break;
      }
      }
    }
  }

  private final MessageHandler mMessageHandler = new MessageHandler();

  private class Task implements Callable<ResultType> {
    private ParameterType mParameter;

    public Task(ParameterType parameter) {
      mParameter = parameter;
    }

    public ResultType call() throws Exception {
      return doInBackground(mParameter);
    }

    @Override
    public int hashCode() {
      return mParameter.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      @SuppressWarnings("unchecked")
      Task other = (Task) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (mParameter == null) {
        if (other.mParameter != null)
          return false;
      } else if (!mParameter.equals(other.mParameter))
        return false;
      return true;
    }

    @SuppressWarnings("rawtypes")
    private RepeatingTask getOuterType() {
      return RepeatingTask.this;
    }

    @Override
    public String toString() {
      return mParameter.toString();
    }
  }

  private final Queue<Task> mTaskQueue = new LinkedList<Task>();

  private class InternRunnable implements Runnable {

    private boolean mRunning = true;
    Task mCurrentCallable;

    public void run() {
      mIsStarted = true;
      while (mRunning) {
        try {
          Message message;
          mCurrentCallable = waitForCallable();
          try {
            TaskMessage<ResultType> messageBody = new TaskMessage<ResultType>(mCurrentCallable.mParameter, null);
            message = mMessageHandler.obtainMessage(MESSAGE_PRE_EXECUTION, messageBody);
            message.sendToTarget();

            Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Executing callable for %s", mCurrentCallable.mParameter));
            ResultType result = mCurrentCallable.call();
            messageBody = new TaskMessage<ResultType>(mCurrentCallable.mParameter, result);
            message = mMessageHandler.obtainMessage(MESSAGE_POST_RESULT, messageBody);
          } catch (Exception e) {
            Log.e(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Something goes wrong while executing callable for %s", mCurrentCallable.mParameter), e);
            TaskMessage<Exception> messageBody = new TaskMessage<Exception>(mCurrentCallable.mParameter, e);
            message = mMessageHandler.obtainMessage(MESSAGE_POST_ERROR, messageBody);
          }
          Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Invoke callback for %s", mCurrentCallable.mParameter));
          message.sendToTarget();
          mCurrentCallable = null;
          mIsCancelled = false;
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    private Task waitForCallable() throws InterruptedException {
      synchronized (mTaskQueue) {
        Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Looking for enqueued task");
        while (mTaskQueue.size() == 0) {
          mIsSleeping = true;
          Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Waiting for enqueued task");
          mTaskQueue.wait();
        }
        mIsSleeping = false;
        Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Returning enqueued task");
        return mTaskQueue.poll();
      }
    }
  }

  private Thread mThread;
  private final InternRunnable mRunnable = new InternRunnable();
  private boolean mIsStarted = false;
  private boolean mIsSleeping = false;
  private boolean mIsStopping = false;
  private boolean mIsCancelled = false;

  protected String mThreadName = CLASS_TAG;

  protected void enqueueTask(ParameterType parameter) {
    if (!mIsStopping) {
      Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Enqueuing %s", parameter));
      synchronized (mTaskQueue) {
        mTaskQueue.add(new Task(parameter));
        Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Notify waiting WorkerThread");
        mTaskQueue.notifyAll();
      }
    }
  }

  protected void removeEnqueuedTask(ParameterType parameter) {
    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Remove enqueued %s", parameter));
    synchronized (mTaskQueue) {
      mTaskQueue.remove(parameter);
    }
  }

  protected void cancelCurrentTask() {
    if(mRunnable.mCurrentCallable != null) {
      mIsCancelled = true;
      mThread.interrupt();
    }
  }

  public final void start() {
    if (mThread == null || mThread.getState() == State.TERMINATED) {
      mIsStarted = false;
      mIsSleeping = false;
      mIsStopping = false;
      mRunnable.mRunning = true;
      mThread = new Thread(mRunnable, mThreadName);
      mThread.setDaemon(false);
      Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Starting WorkerThread");
      mThread.start();
    }
  }

  public final void stop(boolean waitForStopped) throws InterruptedException{
    if (mThread != null) {
      mRunnable.mRunning = false;
      if (waitForStopped) {
        mThread.join();
      }
      mThread = null;
    }
  }

  public final void cancel(boolean waitForCancel) throws InterruptedException {
    if (mThread != null) {
      mIsCancelled = true;
      mRunnable.mRunning = false;
      synchronized (mTaskQueue) {
        mTaskQueue.clear();
      }
      mThread.interrupt();
      if (waitForCancel) {
        mThread.join();
      }
      mThread = null;
    }
  }

  private void finish(ParameterType parameter, ResultType result) {
    if (mIsCancelled) {
      onCancelled(parameter, result);
    } else {
      onPostExecute(parameter, result);
    }

  }

  protected void onPreExecute(ParameterType parameter) {
  }

  protected final void publishProgress(ProgressType progress){
    TaskMessage<ProgressType> messageBody = new TaskMessage<ProgressType>(mRunnable.mCurrentCallable.mParameter, progress);
    Message message = mMessageHandler.obtainMessage(MESSAGE_POST_PROGRESS, messageBody);
    message.sendToTarget();
  }
  
  protected abstract ResultType doInBackground(ParameterType parameter);

  protected abstract void onPostExecute(ParameterType parameter, ResultType result);

  protected void onExceptionThrown(ParameterType parameter, Exception exception) {
  }

  protected void onProgressUpdate(ParameterType parameter, ProgressType progress) {
  }

  protected void onCancelled(ParameterType parameter, ResultType result) {
    onCancelled();
  }

  protected void onCancelled() {
  }

  public final Status getStatus() {
    if (mThread == null || mThread.getState() == State.TERMINATED) {
      return Status.FINISHED;
    }
    if (!mIsStarted) {
      return Status.PENDING;
    }
    if (mIsSleeping) {
      return Status.SLEEPING;
    }
    return Status.RUNNING;
  }

  public boolean isCancelled() {
    return mIsCancelled;
  }

  protected boolean isEnqueued(ParameterType parameter) {
    synchronized (mTaskQueue) {
      return mTaskQueue.contains(new Task(parameter));
    }
  }

  protected boolean isActive(ParameterType parameter) {
    Status status = getStatus();
    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Status: %s", status));
    if (status != Status.RUNNING) {
      return false;
    }
    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("CurrentCallable %s Parameter %s", mRunnable.mCurrentCallable, parameter));
    return mRunnable.mCurrentCallable != null && mRunnable.mCurrentCallable.mParameter.equals(parameter);
  }
}
