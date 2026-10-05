//package org.apache.cxf.interceptor;
//
//import org.apache.cxf.io.CachedOutputStream;
//import org.apache.cxf.message.Message;
//import org.apache.cxf.phase.AbstractPhaseInterceptor;
//import org.apache.cxf.phase.Phase;
//
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//
//public class CachedOutputStreamCloserInterceptor extends AbstractPhaseInterceptor<Message> {
//
//    public CachedOutputStreamCloserInterceptor() {
//        super(Phase.SETUP);
//    }
//
//    private static final String COS_CLEAN_DONE = "org.apache.cxf.cos.clean.done";
//    private static final String COS_TO_CLEAN_LIST = "org.apache.cxf.cos.to.clean.list";
//
//    // Map of the error where CachedOutputStream may be not closed
//    private static final Map<Class<? extends Throwable>, List<String>> KNOWN_ERRORS_MAP = Map.of(
//        java.io.IOException.class, List.of(
//                "Connection reset by peer"
//        )
//    );
//
//    public static void register(Message message, CachedOutputStream cos){
//        if (message == null || cos == null) {
//            return;
//        }
//        List<CachedOutputStream> cosToClean = (List<CachedOutputStream>) message.get(COS_TO_CLEAN_LIST);
//        if (cosToClean == null) {
//            cosToClean = new ArrayList<>();
//            message.put(COS_TO_CLEAN_LIST, cosToClean);
//        }
//        cosToClean.add(cos);
//    }
//
//    @Override
//    public void handleMessage(Message message) throws Fault {
//
//    }
//
//    @Override
//    public void handleFault(Message message) throws Fault {
//        if (message==null || Boolean.TRUE.equals(message.get(COS_CLEAN_DONE))) {
//            return;
//        }
//        Exception ex = message.getContent(Exception.class);
//        List <CachedOutputStream> cosToClean = (List<CachedOutputStream>) message.get(COS_TO_CLEAN_LIST);
//        if (cosToClean!=null && !cosToClean.isEmpty() && mayCosBeNotClosed(ex)) {
//            try {
//                for(CachedOutputStream cos : cosToClean){
//                    try{
//                        cos.close();
//                    }catch (Exception ignored){
//                        // Other exception can be thrown due to the error state of the underlying OutputStream,
//                        // but at least the CachedOutputStream clean itself ( unregister from queue and delete tmp file )
//                        // The .close() force also to log if the callback is attach
//                    }
//                }
//                cosToClean.clear();
//            } catch (Exception ignored) {
//            } finally {
//                message.put(COS_CLEAN_DONE, Boolean.TRUE);
//            }
//        }
//    }
//
//    private boolean mayCosBeNotClosed(Throwable ex) {
//        Throwable cause = ex;
//        while (cause != null) {
//            for (Map.Entry<Class<? extends Throwable>, List<String>> entry : KNOWN_ERRORS_MAP.entrySet()) {
//                Class<? extends Throwable> exceptionClass = entry.getKey();
//                if (exceptionClass.isInstance(cause)) {
//                    String actualMessage = cause.getLocalizedMessage();
//                    if (actualMessage != null) {
//                        List<String> listForThatException = entry.getValue();
//                        if(listForThatException.contains(actualMessage)){
//                            return true;
//                        }
//                    }
//                }
//            }
//            cause = cause.getCause();
//        }
//        return false;
//    }
//}
