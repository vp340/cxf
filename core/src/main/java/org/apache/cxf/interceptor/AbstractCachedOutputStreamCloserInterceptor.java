package org.apache.cxf.interceptor;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.PhaseInterceptorChain;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractCachedOutputStreamCloserInterceptor extends AbstractPhaseInterceptor<Message> {

    protected static final String COS_CLEAN_DONE = "org.apache.cxf.cos.clean.done";
    protected static final String TRACKED_COS_LIST = "org.apache.cxf.cos.tracked.list";

    public AbstractCachedOutputStreamCloserInterceptor(String phase) {
        super(phase);
    }

    protected boolean isCloseEnable(Message message){
        return Boolean.TRUE.equals(message.get(COS_CLEAN_DONE));
    }

    @SuppressWarnings("unchecked")
    protected void closeTrackedCoss(Message message) {
        if (message == null ) {
            return;
        }

        List<CachedOutputStream> trackedCoss = (List<CachedOutputStream>) message.get(TRACKED_COS_LIST);

        if (trackedCoss != null && !trackedCoss.isEmpty()) {
            for (CachedOutputStream cos : trackedCoss) {
                if (cos != null) {
                    try {
                        cos.close();
                    } catch (Exception ignored) {
                    }
                }
            }
            trackedCoss.clear();
        }
    }

    protected void markAsDone(Message message){
        message.put(COS_CLEAN_DONE, Boolean.TRUE);
    }

    public static class CachedOutputStreamCloserCallback implements CachedOutputStreamCallback {

        @Override
        @SuppressWarnings("unchecked")
        public void onFlush(CachedOutputStream cos) {
            if (cos == null) return;

            Message currentMessage = PhaseInterceptorChain.getCurrentMessage();
            if (currentMessage != null) {
                List<CachedOutputStream> trackedCoss = (List<CachedOutputStream>) currentMessage.get(TRACKED_COS_LIST);
                if (trackedCoss == null) {
                    trackedCoss = new CopyOnWriteArrayList<>();
                    currentMessage.put(TRACKED_COS_LIST, trackedCoss);
                }
                if (!trackedCoss.contains(cos)) {
                    trackedCoss.add(cos);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onClose(CachedOutputStream cos) {
            if (cos == null) return;

            Message currentMessage = PhaseInterceptorChain.getCurrentMessage();
            if (currentMessage != null) {
                List<CachedOutputStream> trackedCoss = (List<CachedOutputStream>) currentMessage.get(TRACKED_COS_LIST);
                if (trackedCoss != null) {
                    trackedCoss.remove(cos);
                }
            }
        }
    }
}
