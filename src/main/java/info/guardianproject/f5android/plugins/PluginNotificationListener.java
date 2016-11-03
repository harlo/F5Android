package info.guardianproject.f5android.plugins;

public interface PluginNotificationListener {
	public void onUpdate(String with_message);
	public void onFailure();
}
