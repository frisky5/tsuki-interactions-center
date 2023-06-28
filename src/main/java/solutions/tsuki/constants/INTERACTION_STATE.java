package solutions.tsuki.constants;

public interface INTERACTION_STATE {

  int NEW = 0;
  int QUEUED = 1;

  int OFFERING_TO_AGENT = 2;
  int OFFERING_TO_AGENT_ACCEPTED = 3;
  int OFFERING_TO_AGENT_DECLINED = 4;

  int QUEUED_AFTER_OFFERING_TO_AGENT_DECLINED = 5;

  int[] ALLOWED_TO_ENQUEUE = new int[]{NEW, OFFERING_TO_AGENT_DECLINED};
}
