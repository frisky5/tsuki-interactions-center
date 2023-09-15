package solutions.tsuki.ic.agents.constants;

public interface AGENT_STATES {

  int LOGGED_OUT = 0;
  int NOT_READY = 1;
  int READY = 2;
  int BUSY = 3;
  int BREAK = 4;
  int OFFERING_AN_INTERACTION = 6;

  int[] ALLOWED_TO_LOGOUT = new int[]{ NOT_READY, READY, BREAK,OFFERING_AN_INTERACTION};
  int[] ALLOWED_TO_LOGIN = new int[]{LOGGED_OUT};
  int[] ALLOWED_TO_READY = new int[]{NOT_READY, BREAK };
  int[] ALLOWED_TO_NOT_READY = new int[]{LOGGED_OUT,READY, BREAK,OFFERING_AN_INTERACTION};

  int[] ALLOWED_TO_OFFER_INTERACTION = new int[]{READY};
  int[] ALLOWED_TO_BREAK = new int[]{READY, NOT_READY};

}
