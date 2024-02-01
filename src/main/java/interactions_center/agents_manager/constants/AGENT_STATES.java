package interactions_center.agents_manager.constants;

import java.util.HashMap;

public interface AGENT_STATES {

    int LOGOUT = 0;
    int NOT_READY = 1;
    int READY = 2;

    int BREAK = 4;
    int WORKING = 5;
    int OFFERING_INTERACTION = 6;
    int OFFERING_INTERACTION_TRANSFER = 7;
    int OFFERING_INTERACTION_COLLAB = 8;

    HashMap<Integer, String> STATE_TO_NAME_MAP = new HashMap<>() {{
        put(LOGOUT, "Logged Out");
        put(NOT_READY, "Not Ready");
        put(BREAK, "Break");
        put(WORKING, "Working");
        put(OFFERING_INTERACTION, "Offering Interaction");
        put(OFFERING_INTERACTION_TRANSFER, "Offering Interaction Transfer");
        put(OFFERING_INTERACTION_COLLAB, "Offering Interaction Collab");

    }};

    int[] ALLOWED_TO_LOGOUT = new int[]{NOT_READY, READY, BREAK};
    int[] ALLOWED_TO_LOGIN = new int[]{LOGOUT};
    int[] ALLOWED_TO_READY = new int[]{NOT_READY, BREAK, LOGOUT};
    int[] ALLOWED_TO_NOT_READY = new int[]{LOGOUT, READY, BREAK, OFFERING_INTERACTION};

    int[] ALLOWED_TO_OFFER_INTERACTION = new int[]{READY};
    int[] ALLOWED_TO_BREAK = new int[]{READY, NOT_READY};

}
