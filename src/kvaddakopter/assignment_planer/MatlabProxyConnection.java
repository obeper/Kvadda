package kvaddakopter.assignment_planer;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.MatlabProxyFactoryOptions.Builder;

/**
 * Sets up an Matlab Proxy, which can be called for function calls to Matlab.
 * @author tobiashammarling
 *
 */
public class MatlabProxyConnection {
	MatlabProxy proxy;

	/**
	 * Starts the Matlab Proxy Server, if to be running in the background. Set option to <br>
	 * "quiet". To try connect with an existing proxy set option to "existing". Otherwise <br>
	 * the session will be started in regular mode. The default setting is in quiet mode.
	 * @param option
	 * @throws MatlabConnectionException
	 */
	public void startMatlab(String option) {
		System.out.println("Setting up the Matlab proxy");
		
		//Create a proxy, which will be used to control MATLAB
		if (option.equals("quiet")) {
			//If option is set to quiet the Matlab session will start quiet
			//and be running in the background
			System.out.println("Setting up a quiet Matlab session ...");
			Builder buildoptions = new MatlabProxyFactoryOptions.Builder();
			buildoptions.setHidden(true);
			MatlabProxyFactoryOptions options = buildoptions.build();

			MatlabProxyFactory factory = new MatlabProxyFactory(options);
			try {
				this.proxy = factory.getProxy();
			} catch (MatlabConnectionException e) {
				e.printStackTrace();
			}
		}
		else if (option.equals("existing")) {
			//If option is set to existing the proxy will try to connect to
			//to an running session of Matlab
			System.out.println("Connecting to running session of Matlab ...");
			Builder buildoptions = new MatlabProxyFactoryOptions.Builder();
			buildoptions.setUsePreviouslyControlledSession(true);
			MatlabProxyFactoryOptions options = buildoptions.build();

			MatlabProxyFactory factory = new MatlabProxyFactory(options);
			try {
				this.proxy = factory.getProxy();
			} catch (MatlabConnectionException e) {
				e.printStackTrace();
			}
		}
		else {
			Builder buildoptions = new MatlabProxyFactoryOptions.Builder();
			MatlabProxyFactoryOptions options = buildoptions.build();
			
			MatlabProxyFactory factory = new MatlabProxyFactory(options);
			try {
				this.proxy = factory.getProxy();
			} catch (MatlabConnectionException e) {
				e.printStackTrace();
			}
		}
		
		//Go to the correct directory
		try {
			proxy.eval("cd('" + System.getProperty("user.dir") + 
					"/src/kvaddakopter/assignment_planer/Matlab')");
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Terminates the assigned Proxy Server.
	 */
	public void terminateMatlab() {
		//Terminate Matlab
		//proxy.exit();

		//Disconnect the proxy from MATLAB
		this.proxy.disconnect();
	}

	/**
	 * Returns the current proxy.
	 * @return
	 */
	public MatlabProxy getMatlabProxy() {
		return this.proxy;
	}
}
