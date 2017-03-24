package softuniBlog.service;

public interface NotificationService {
    void addInfoMessage(String msg);
    void addWarningMessage(String msg);
    void addErrorMessage(String msg);
}
