package net.f5.crypt;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

public class F5Random {
    private SecureRandom random = null;

    private byte[] b = null;
    
    public F5Random() {
    	this(null);
    }

	public F5Random(String password) {
		for(Provider p : Security.getProviders()) {
            System.out.println("PROVIDER: " + p.getName());
        }
        this.random = new SecureRandom();
        try {
        	this.random.setSeed(password.getBytes());
        } catch(NullPointerException e) {
        	this.random.setSeed(new String("helloworld").getBytes());
        }
        
        this.b = new byte[1];
    }

    // get a random byte
    public int getNextByte() {
        this.random.nextBytes(this.b);
        return this.b[0];
    }

    // get a random integer 0 ... (maxValue-1)
    public int getNextValue(final int maxValue) {
        int retVal = getNextByte() | getNextByte() << 8 | getNextByte() << 16 | getNextByte() << 24;
        retVal %= maxValue;
        if (retVal < 0) {
            retVal += maxValue;
        }
        return retVal;
    }
}
