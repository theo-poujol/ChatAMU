package chatamu.protocol;

public class Protocol {

    public enum PREFIX {
        LOGIN, //0
        ERROR, //1
        MESSAGE, //2
        ERR_LOG, //3
        ERR_MSG; //4


        @Override
        public String toString() {
            switch (this) {
                case ERROR:
                    return "ERROR ";
                case LOGIN:
                    return "LOGIN ";
                case MESSAGE:
                    return "MESSAGE ";
                default:
                    return "";
            }
        }
    }



}
