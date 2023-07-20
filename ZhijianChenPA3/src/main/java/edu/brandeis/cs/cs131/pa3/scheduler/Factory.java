package edu.brandeis.cs.cs131.pa3.scheduler;

import java.util.Collection;

import edu.brandeis.cs.cs131.pa3.submission.BasicTunnel;
import edu.brandeis.cs.cs131.pa3.submission.PriorityScheduler;
import edu.brandeis.cs.cs131.pa3.tunnel.Car;
import edu.brandeis.cs.cs131.pa3.tunnel.Direction;
import edu.brandeis.cs.cs131.pa3.tunnel.Sled;
import edu.brandeis.cs.cs131.pa3.tunnel.Tunnel;
import edu.brandeis.cs.cs131.pa3.tunnel.Vehicle;

/**
 * The interface that creates instances of specific classes
 * @author cs131a
 *
 */
public class Factory {

	/**
	 * Creates a new instance of class BasicTunnel
	 * @param name the name of the tunnel to create
	 * @return the newly created instance of the BasicTunnel class
	 */
	public Tunnel createNewBasicTunnel(String name){
		return new BasicTunnel(name);
}
    
    /**
     * Creates a new instance of class PriorityScheduler
     * @param name the name of the priority scheduler to create
     * @param tunnels the collection of tunnels that the scheduler should manage
     * @return the newly created instance of the PriorityScheduler class
     */
	public Scheduler createNewPriorityScheduler(String name, Collection<Tunnel> tunnels){
		return new PriorityScheduler(name, tunnels);
	}
    
    /**
     * Creates a new instance of class Car
     * @param name the name of the car to create
     * @param direction the direction of the car
     * @return the newly created instance of the Car class
     */
    public Vehicle createNewCar(String name, Direction direction){
		return new Car(name, direction); 
    }

    /**
     * Creates a new instance of class Sled
     * @param name the name of the sled to create
     * @param direction the direction of the sled
     * @return the newly created instance of the Sled class
     */
    public Vehicle createNewSled(String name, Direction direction){
		return new Sled(name, direction);  
    }

}
