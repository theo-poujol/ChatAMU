public class Protocol {

    public enum PREFIX {
        LOGIN,
        ERROR,
        MESSAGE,
        ERR_LOG,
        ERR_MSG;


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
