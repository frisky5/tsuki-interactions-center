package interactions_center.agents_manager.json.responses;

import java.util.List;

public class Agent {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private List<Queue> assignedQueues;
    private List<Group> groups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Queue> getAssignedQueues() {
        return assignedQueues;
    }

    public void setAssignedQueues(List<Queue> assignedQueues) {
        this.assignedQueues = assignedQueues;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
