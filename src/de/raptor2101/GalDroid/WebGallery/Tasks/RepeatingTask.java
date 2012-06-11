package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class RepeatingTask<ParameterType, ProgressType, ResultType> implements TaskInterface {
  protected static final String CLASS_TAG = "RepeatingTask";
  private static final int MESSAGE_POST_RESULT = 1;
  private static final int MESSAGE_POST_PROGRESS = 2;
  private static final int MESSAGE_POST_ERROR = 3;
  private static final int MESSAGE_PRE_EXECUTION = 4;

  private final int mMaxEnqueuedTasks;
  
  public RepeatingTask(int maxEnquedTasks) {
    mMaxEnqueuedTasks = maxEnquedTasks;
  }
  
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
    private final ParameterType mCallParameter;

    public Task(ParameterType callParameter) {
      mCallParameter = callParameter;
    }

    public ResultType call() throws Exception {
      return doInBackground(mCallParameter);
    }

    @Override
    public int hashCode() {
      return mCallParameter.hashCode();
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
      if (mCallParameter == null) {
        if (other.mCallParameter != null)
          return false;
      } else if (!mCallParameter.equals(other.mCallParameter))
        return false;
      return true;
    }

    @SuppressWarnings("rawtypes")
    private RepeatingTask getOuterType() {
      return RepeatingTask.this;
    }

    @Override
    public String toString() {
      return mCallParameter.toString();
    }
  }

  protected final LinkedList<Task> mTaskQueue = new LinkedList<Task>();

  private class InternRunnable implements Runnable {

    boolean mRunning = true;
    Task mCurrentCallable;
    long mThreadID;
    public void run() {
      mThreadID = Thread.currentThread().getId();
      Log.d(this.buildLogTag(), "Thread started");
      mIsStarted = true;
      while (mRunning) {
        try {
          Message message;
          mCurrentCallable = waitForCallable();
          try {
            TaskMessage<ResultType> messageBody = new TaskMessage<ResultType>(mCurrentCallable.mCallParameter, null);
            message = mMessageHandler.obtainMessage(MESSAGE_PRE_EXECUTION, messageBody);
            message.sendToTarget();

            Log.d(this.buildLogTag(), String.format("Executing Task(%s)", mCurrentCallable.mCallParameter));
            ResultType result = mCurrentCallable.call();
            messageBody = new TaskMessage<ResultType>(mCurrentCallable.mCallParameter, result);
            message = mMessageHandler.obtainMessage(MESSAGE_POST_RESULT, messageBody);
          } catch (Exception e) {
            Log.e(this.buildLogTag(), String.format("Something goes wrong while executing Task(%s)", mCurrentCallable.mCallParameter), e);
            TaskMessage<Exception> messageBody = new TaskMessage<Exception>(mCurrentCallable.mCallParameter, e);
            message = mMessageHandler.obtainMessage(MESSAGE_POST_ERROR, messageBody);
          }
          Log.d(buildLogTag(), String.format("Invoke Callback for Task(%s)", mCurrentCallable.mCallParameter));
          message.sendToTarget();
          mCurrentCallable = null;
          mIsCancelled = false;
        } catch (InterruptedException e) {
          Log.d(buildLogTag(), "got interrupted");
        }
      }
      Log.d(this.buildLogTag(), "Thread finished");
    }

    private String buildLogTag() {
      return String.format("%s (%d:%s)", CLASS_TAG, mThreadID, mThreadName);
    }
    
    private Task waitForCallable() throws InterruptedException {
      synchronized (mTaskQueue) {
        Log.d(this.buildLogTag(), String.format("Looking for enqueued task, QueueSize: %d",mTaskQueue.size()));
        while (mTaskQueue.size() == 0) {
          mIsSleeping = true;
          Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Waiting for enqueued task");
          mTaskQueue.wait();
        }
        mIsSleeping = false;
        Log.d(this.buildLogTag(), String.format("Returning enqueued task, QueueSize: %d",mTaskQueue.size()));
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
      Log.d(buildLogTag(), String.format("Enqueuing %s , QueueSize: %d", parameter, mTaskQueue.size()));
      synchronized (mTaskQueue) {
        mTaskQueue.add(new Task(parameter));
        
        while (mMaxEnqueuedTasks > 0 && mTaskQueue.size() > mMaxEnqueuedTasks) {
          mTaskQueue.poll();
        }
        
        Log.d(buildLogTag(), "Notify waiting WorkerThread");
        mTaskQueue.notifyAll();
      }
    }
  }

  private String buildLogTag() {
    long threadID = mThread != null? mThread.getId() : -1;
    return String.format("%s (%d:%s)", CLASS_TAG, threadID, mThreadName);
  }

  protected void removeEnqueuedTask(ParameterType parameter) {
    Log.d(buildLogTag(), String.format("Remove enqueued %s", parameter));
    synchronized (mTaskQueue) {
      mTaskQueue.remove(parameter);
    }
  }

  protected void cancelCurrentTask(boolean waitForCancel) throws InterruptedException {
    if(mRunnable.mCurrentCallable != null) {
      mIsCancelled = true;
      mThread.interrupt();
    }
    
    while(waitForCancel && mIsCancelled) {
      mThread.interrupt();
      Thread.sleep(100);
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
      Log.d(buildLogTag(), "Starting WorkerThread");
      mThread.start();
    }
  }

  public final void stop(boolean waitForStopped) throws InterruptedException{
    if (mThread != null) {
      mRunnable.mRunning = false;
      if(mIsSleeping) {
        mThread.interrupt();
      }
      
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

  protected void onPreExecute(ParameterType callParameter) {
  }

  protected final void publishProgress(ProgressType progress){
    TaskMessage<ProgressType> messageBody = new TaskMessage<ProgressType>(mRunnable.mCurrentCallable.mCallParameter, progress);
    Message message = mMessageHandler.obtainMessage(MESSAGE_POST_PROGRESS, messageBody);
    message.sendToTarget();
  }
  
  protected abstract ResultType doInBackground(ParameterType callParameter);

  protected abstract void onPostExecute(ParameterType parameter, ResultType result);

  protected void onExceptionThrown(ParameterType callParameter, Exception exception) {
  }

  protected void onProgressUpdate(ParameterType callParameter, ProgressType progress) {
  }

  protected void onCancelled(ParameterType callParameter, ResultType result) {
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

  protected boolean isEnqueued(ParameterType callParameter) {
    synchronized (mTaskQueue) {
      return mTaskQueue.contains(new Task(callParameter));
    }
  }

  protected boolean isActive(ParameterType parameter) {
    Status status = getStatus();
    if (status != Status.RUNNING) {
      return false;
    }
    return mRunnable.mCurrentCallable != null && mRunnable.mCurrentCallable.mCallParameter.equals(parameter);
  }

  protected ParameterType getEnqueuedTask(ParameterType callParameter) {
    synchronized (mTaskQueue) {
      int index = mTaskQueue.indexOf(callParameter);
      if(index > -1) {
        return mTaskQueue.get(index).mCallParameter;
      } else {
        return null;
      }
    }
  }
  
  protected ParameterType getEnqueuedTask(int index) {
    synchronized (mTaskQueue) {
      return mTaskQueue.get(index).mCallParameter;
    }
  }
  
  protected int getEnqueuedTaskPosition(ParameterType callParameter) {
    synchronized (mTaskQueue) {
      return mTaskQueue.indexOf(callParameter);
    }
  }
  
  protected ParameterType getActiveTask() {
    if(mRunnable.mCurrentCallable != null) {
      return mRunnable.mCurrentCallable.mCallParameter;
    } else {
      return null;
    }
      
  }
}
