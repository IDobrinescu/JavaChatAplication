import java.util.ArrayList;

public interface MesageHandler {
    void onMesageReceived(String msg);
    void onArrayReceived(ArrayList<String> arrayList);
}
