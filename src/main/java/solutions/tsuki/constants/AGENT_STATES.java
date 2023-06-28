package solutions.tsuki.constants;

public interface AGENT_STATES {

  int LOGGED_OUT = 0;
  int LOGGED_IN = 1;
  int NOT_READY = 2;
  int READY = 3;
  int BUSY = 4;
  int BREAK = 5;
  int OFFERING_AN_INTERACTION = 6;

  int[] ALLOWED_TO_LOGOUT = new int[]{LOGGED_IN, NOT_READY, READY, BREAK,OFFERING_AN_INTERACTION};
  int[] ALLOWED_TO_LOGIN = new int[]{LOGGED_OUT};
  int[] ALLOWED_TO_READY = new int[]{NOT_READY, BREAK, LOGGED_IN};
  int[] ALLOWED_TO_NOT_READY = new int[]{READY, BREAK, LOGGED_IN,OFFERING_AN_INTERACTION};

  int[] ALLOWED_TO_OFFER_INTERACTION = new int[]{READY};
  int[] ALLOWED_TO_BREAK = new int[]{READY, NOT_READY, LOGGED_IN};

}
