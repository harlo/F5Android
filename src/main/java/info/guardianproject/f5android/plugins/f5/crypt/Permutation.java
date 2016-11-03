package info.guardianproject.f5android.plugins.f5.crypt;

import info.guardianproject.f5android.plugins.f5.F5Buffers;
import info.guardianproject.f5android.stego.StegoProcessThread;

public class Permutation {
    // The constructor of class Permutation creates a shuffled
    // sequence of the integers 0 ... (size-1).
	F5Buffers f5;
	
    public Permutation(final int size, final F5Random random, F5Buffers f5, StegoProcessThread thread_monitor) {
        int i, randomIndex, tmp;
        if(thread_monitor.isInterrupted()) { return; }
        
        this.f5 = f5;
        this.f5.initF5Permutation(size);
        
        // To create the shuffled sequence, we initialise an array
        // with the integers 0 ... (size-1).
        for (i = 0; i < size; i++) {
            // initialise with �size� integers
        	this.f5.setPermutationValues(new int[] {i}, i);
        }
        
        this.f5.onUpdate(null);
        
        if(thread_monitor.isInterrupted()) { return; }
        int maxRandom = size; // set number of entries to shuffle
        for (i = 0; i < size; i++) { // shuffle entries
            randomIndex = random.getNextValue(maxRandom--);
            tmp = this.f5.getPermutationValues(randomIndex);
            this.f5.setPermutationValues(new int[] { this.f5.getPermutationValues(maxRandom) }, randomIndex);
            this.f5.setPermutationValues(new int[] { tmp }, maxRandom);
        }
        
        this.f5.onUpdate(null);
    }

    // get value #i from the shuffled sequence
    public int getShuffled(final int i) {
        return this.f5.getPermutationValues(i);
    }
}
