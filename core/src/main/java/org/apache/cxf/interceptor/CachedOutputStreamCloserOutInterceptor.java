package org.apache.cxf.interceptor;

import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CachedOutputStreamCloserOutInterceptor extends AbstractCachedOutputStreamCloserInterceptor {

    private static final String COS_CLEAN_DONE = "org.apache.cxf.cos.clean.done";


    class CachedOutputStreamCloserFaultOutInterceptor extends AbstractPhaseInterceptor<Message> {
        CachedOutputStreamCloserFaultOutInterceptor() {
            super(Phase.SETUP);
        }

        @Override
        public void handleMessage(Message message) throws Fault {
        }

        @Override
        public void handleFault(Message message) throws Fault {
            CachedOutputStreamCloserOutInterceptor.this.handleMessage(message);
        }
    }

    public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
        Collection<PhaseInterceptor<? extends Message>> ret = new ArrayList<>();
        ret.add(new CachedOutputStreamCloserFaultOutInterceptor());
        return ret;
    }

    public CachedOutputStreamCloserOutInterceptor() {
        super(Phase.WRITE_ENDING);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if(!isCloseEnable(message)){
            return;
        }
        closeTrackedCoss(message);
        markAsDone(message);
    }
}
