package chatamu.protocol;

public class Protocol {

    public enum PREFIX {
        LOGIN, //0
        ERROR, //1
        MESSAGE, //2
        DCNTD, //3
        ERR_LOG, //4
        ERR_MSG; //5


        @Override
        public String toString() {
            switch (this) {
                case ERROR:
                    return "ERROR ";
                case LOGIN:
                    return "LOGIN ";
                case MESSAGE:
                    return "MESSAGE ";
                case DCNTD:
                    return "DISCONNECTED";
                case ERR_LOG:
                    return "_ERR_LOG_C3";
                case ERR_MSG:
                    return "_ERR_MSG_C4";
                default:
                    return "";
            }
        }
    }



}
