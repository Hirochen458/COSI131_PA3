/**
 * Zhijian Chen
 * Chen5340@brandeis.edu
 * Apr 3th 2023
 * PA3
 * This class is used to implement the PriotirySchedulaer function that schedule the vehicles based on their priority.
 * Known bugs: None.
 */


package edu.brandeis.cs.cs131.pa3.submission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import edu.brandeis.cs.cs131.pa3.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa3.tunnel.Tunnel;
import edu.brandeis.cs.cs131.pa3.tunnel.Vehicle;

/**
 * The priority scheduler assigns vehicles to tunnels based on their priority.
 * It extends the Scheduler class.
 */
public class PriorityScheduler extends Scheduler{
	
	//The PriorityQuene used to keep the waiting vehicles.
	public PriorityQueue<Vehicle> waitingQueue = new PriorityQueue<>();
	
	//The hash map used to keep track of vehicles in tunnel.
	public HashMap<Vehicle, Tunnel> vehicleInside = new HashMap<Vehicle, Tunnel>();
	
	//The array list of tunnels.
	public ArrayList<Tunnel> tunnelList = new ArrayList<>(tunnels);
	
	//The lock used to implement the mutual exclusion
	private final ReentrantLock lock = new ReentrantLock();
	
	private final Condition tunnelIsFull = lock.newCondition();
	/**
	 * Creates an instance of a priority scheduler with the given name and tunnels
	 * @param name the name of the priority scheduler
	 * @param tunnels the tunnels where the vehicles will be scheduled to
	 */
	public PriorityScheduler(String name, Collection<Tunnel> tunnels) {
		super(name, tunnels);
		//public ArrayList<Tunnel> tunnelList = new ArrayList<>(tunnels);
	}
	
	
	/**
	 * It check if the vehicle is able to get into a tunnel, or keep the vehicle waiting if not.
	 * @param vehicle that used to be checked.
	 */
	@Override
	public Tunnel admit(Vehicle vehicle) {
		lock.lock();
		Tunnel entered = null;
		try {
			waitingQueue.add(vehicle);
			boolean success = false;
			
			while(success==false) {
				if(waitingQueue.isEmpty()||waitingQueue.peek().getVehiclePriority()<=vehicle.getVehiclePriority()) {
					for(int i = 0; i < tunnelList.size();i++) {
						if(tunnelList.get(i).tryToEnter(vehicle)) {
							vehicleInside.put(vehicle, tunnelList.get(i));
							waitingQueue.remove(vehicle);
							success=true;
							entered = tunnelList.get(i);
							break;
						}
					}
				}
				if(success==false) {
					tunnelIsFull.await();
				}

			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock.unlock();
		return entered;

		
		
		
	}
	/**
	 * This method pull vehicle out of tunnel if it's finished.
	 * @param the vehicle that is finished.
	 */
	@Override
	public void exit(Vehicle vehicle) {
		lock.lock();
		for(int i = 0; i < tunnelList.size();i++) {
			if(vehicleInside.containsKey(vehicle)) {
				vehicleInside.get(vehicle).exitTunnel(vehicle);
				vehicleInside.remove(vehicle);
				tunnelIsFull.signalAll();
				break;
			}
			
		}
		lock.unlock();
	}
	
}
