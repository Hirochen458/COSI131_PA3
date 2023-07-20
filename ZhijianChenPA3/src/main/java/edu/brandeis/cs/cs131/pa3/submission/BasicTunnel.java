/**
 * Zhijian Chen
 * Chen5340@brandeis.edu
 * Apr 3th 2023
 * PA3
 * This class is used to implement the BasicTunnel function that let vehicle enter and exit.
 * Known bugs: None.
 */


package edu.brandeis.cs.cs131.pa3.submission;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cs.cs131.pa3.tunnel.Car;
import edu.brandeis.cs.cs131.pa3.tunnel.Sled;
import edu.brandeis.cs.cs131.pa3.tunnel.Tunnel;
import edu.brandeis.cs.cs131.pa3.tunnel.Vehicle;

/**
 * The BasicTunnel enforces a basic admittance policy.
 * It extends the Tunnel class.
 */
public class BasicTunnel extends Tunnel {
	
	//Used to keep track the direction of vehicles in tunnel
	public String direction = "";
	
	//Used to keep track of the number of cars in tunnel
	public int numOfCar = 0;
	
	//Used to keep track of the number of sleds in tunnel;
	public int numOfSled = 0;
	
	//Used to keep track of all vehicles in tunnel
	public List<Vehicle> list=new ArrayList<Vehicle>();

	/**
	 * Creates a new instance of a basic tunnel with the given name
	 * @param name the name of the basic tunnel
	 */
	public BasicTunnel(String name) {
		super(name);
	}

	/**
	 * This method is used to check if a vehicle is eligible to enter this tunnel
	 * @param the vehicle that need to enter the tunnel
	 * @return true if able to enter false otherwise
	 */
	@Override
	protected boolean tryToEnterInner(Vehicle vehicle) {
		if(vehicle instanceof Car) {
			//if tunnel is empty
			if(numOfCar ==0 && numOfSled ==0) {
				list.add(vehicle);
				direction = vehicle.getDirection().toString();
				numOfCar++;
				return true;
			}
			//if tunnel is not empty with vehicle inside
			else if(numOfCar < 3 && numOfSled == 0) {
				if(vehicle.getDirection().toString().equals(direction)) {
					list.add(vehicle);
					numOfCar++;
					return true;
				}else {
					return false;
				}
			}else {
				return false;
			}
		}else if(vehicle instanceof Sled){
			//tunnel must be empty for sled
			if(numOfCar ==0 && numOfSled ==0) {
				list.add(vehicle);
				direction = vehicle.getDirection().toString();
				numOfSled++;
				return true;
			}else {
				return false;
			}
		}
		return false;
	}

	
	/**
	 * This method is used to update the tunnel and let vehicle to exit the tunnel and update the tunnel
	 * @param the vehicle that need to exit the tunnel
	 */
	@Override
	protected void exitTunnelInner(Vehicle vehicle) {
		if(vehicle instanceof Car) {
			list.remove(vehicle);
			numOfCar--;
			if(list.isEmpty()) {
				direction ="";
			}
		}else if(vehicle instanceof Sled) {
			list.remove(vehicle);
			numOfSled--;
			if(list.isEmpty()) {
				direction ="";
			}
		}
	}

}
