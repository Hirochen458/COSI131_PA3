package edu.brandeis.cs.cs131.pa3;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import edu.brandeis.cs.cs131.pa3.logging.Event;
import edu.brandeis.cs.cs131.pa3.logging.EventType;
import edu.brandeis.cs.cs131.pa3.logging.Log;
import edu.brandeis.cs.cs131.pa3.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa3.tunnel.Direction;
import edu.brandeis.cs.cs131.pa3.tunnel.Sled;
import edu.brandeis.cs.cs131.pa3.tunnel.Tunnel;
import edu.brandeis.cs.cs131.pa3.tunnel.Vehicle;

public class PrioritySchedulerTest {

	private final String prioritySchedulerName = "SCHEDULER";

	private static final int NUM_RUNS = 10;

	@BeforeEach
	public void setUp() {
		Tunnel.DEFAULT_LOG.clearLog();
	}

	@BeforeAll
	public static void broadcast() {
		System.out.printf("Running Priority Scheduler Tests using %s \n",
				TestUtilities.factory.getClass().getCanonicalName());
	}

	private Scheduler setupSimplePriorityScheduler(String name) {
		Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
		tunnels.add(TestUtilities.factory.createNewBasicTunnel(name));
		return TestUtilities.factory.createNewPriorityScheduler(prioritySchedulerName, tunnels);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarEnter() {
		Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.random());
		Scheduler scheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(car, scheduler);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testSledEnter() {
		Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[0], Direction.random());
		Scheduler scheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(sled, scheduler);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testPriorityConstraint() {
		List<Thread> vehicleThreads = new ArrayList<Thread>();
		Scheduler priorityScheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		for (int i = 0; i < 7; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			car.setScheduler(priorityScheduler);
			if (i < 3) {
				car.setVehiclePriority(4);
			} else {
				car.setVehiclePriority(i - 3);
			}
			car.setScheduler(priorityScheduler);
			car.start();
			vehicleThreads.add(car);
		}
		for (Thread t : vehicleThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		int i = 0;
		Vehicle lastEnteredVehicle = null;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS) {
				if (i++ > 2) {
					if (lastEnteredVehicle == null) {
						lastEnteredVehicle = currentEvent.getVehicle();
					} else if (currentEvent.getVehicle().getPriority() > lastEnteredVehicle.getPriority()) {
						assertTrue(false, "Vehicle " + currentEvent.getVehicle() + " has higher priority than "
								+ lastEnteredVehicle + " and should run before!");
					}
				}
			}
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		assertTrue(i==7, "Expected 7 vehicles to enter the tunnels successfully, however only " + i + " did.");
	}

	/**
	 * Test to make sure that the priority is checked properly before any admittance
	 * by the PriorityScheduler. Scheduler should never try to admit a Vehicle
	 * without first checking if its the top priority waiting vehicle first.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testProperPriorityCheck() {
		Scheduler priorityScheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		Vehicle[] cars = new Vehicle[3];

		// Create 3 Cars (and their corresponding Threads). car 0 has priority 4, car 1
		// has priority 2, car 2 has priority 0. All 3 of them will be slow.
		// use 64 bits to store 0, 1, or 2.
		for (long l = 0; l < 3; l++) {
		    int i = (int) l;
		    cars[i] = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
		    cars[i].setScheduler(priorityScheduler);
		    cars[i].setVehiclePriority(2 * (2 - i));
		    cars[i].setSpeed(0);
		}

		// Create sled with priority 1 (and corresponding Thread). Speed doesn't matter
		Vehicle sled = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		sled.setScheduler(priorityScheduler);
		sled.setVehiclePriority(1);

		// Start up cars 0 and 1, sleep a bit to let threads drive in the tunnel. Above
		// we made both these cars (very) slow so it is highly unlikely that the cars
		// will finish before a context switch back to the test thread.
		cars[0].start();
		cars[1].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Start up the sled thread. I pause before starting the last car to make sure
		// that we try to admit the (higher priority) sled before the (lower priority)
		// third car.
		sled.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cars[2].start();

		// block until all vehicles have finished traveling
		try {
			sled.join();
			for (Thread carThread : cars) {
				carThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;

		boolean sledLeft = false;
		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();
//			System.out.println(currentEvent);

			if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {

				// Mark that the sled is left if the event occurred
				if (currentEvent.getVehicle() instanceof Sled) {
					sledLeft = true;
				}
				// Make sure that the sled has already left before the third car left
				else if (currentEvent.getVehicle().equals(cars[2])) {
					assertTrue(sledLeft, " car 2 (priority 0) left before sled (priority 1)");
				}
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		// make sure all vehicles completed
		for (Vehicle car : cars) {
			assertTrue(completed.contains(car), car + " did not complete");
		}
		assertTrue(completed.contains(sled), sled + " did not complete");
	}
	
	/**
	 * Test to make sure that the Cars are never forced to wait unnecessarily. 
	 * The scheduler should always allow a top priority car to enter a tunnel if
	 * the tunnel's admittance policy is satisfied. In this situation, a north bound
	 * car enters the tunnel, then a north bound vehicle and a south bound vehicle attempt to enter.
	 * The north bound vehicle should never be denied admittance to the tunnel as long as
	 * there is currently 1 north bound vehicle in the tunnel.
	 * 
	 * @author Eitan Joseph
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarsDontWaitUnnecessarily() {
		Scheduler priorityScheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		Vehicle[] cars = new Vehicle[3];

		// Create 3 Cars with equal priority. Two go North, one goes South.
		// use 64 bits to store 0, 1, or 2.
		for (long l = 0; l < 3; l++) {
		    int i = (int) l;
		    cars[i] = TestUtilities.factory.createNewCar(Integer.toString(i), (l%2 == 0) ? Direction.NORTH : Direction.SOUTH);
		    cars[i].setScheduler(priorityScheduler);
		    cars[i].setSpeed(0);
		}

		// Start up North car then sleep
		cars[0].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Start up South car then give it time to try to enter
		cars[1].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Start up North car
		cars[2].start();
		

		for (Vehicle car: cars)
			try {
				car.join();
			} catch (InterruptedException e) {
				fail("Unexpected Interrupt");
			}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;

		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();

			if (currentEvent.getEvent().equals(EventType.ENTER_FAILED)) {

				// Make sure that the second north bound car never has to wait
				if (currentEvent.getVehicle().equals(cars[2])) {
					fail("Car 2 (North) was forced to wait when only Car 0 (North) was in the Tunnel");
				}
			}
			
			if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		// make sure all vehicles completed
		for (Vehicle car : cars) {
			assertTrue(completed.contains(car), car + " did not complete");
		}
	}
	
	/**
	 * Test to make sure that the Cars are never forced to wait unnecessarily. 
	 * The scheduler should always allow a top priority car to enter a tunnel if
	 * the tunnel's admittance policy is satisfied. In this situation, a slow car enters
	 * the tunnel, then a sled attempts to enter, then another car attempts to enter.
	 * The second car should never have to wait for the sled if all Vehicles have equal priority.
	 * 
	 * @author Eitan Joseph
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarsDontWaitUnnecessarilyForSleds() {
		Scheduler priorityScheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		Vehicle[] vehicles = new Vehicle[3];

		// Create 3 Vehicles with equal priority. Two are Cars, one is a Sled
		// use 64 bits to store 0, 1, or 2.
		for (long l = 0; l < 3; l++) {
		    int i = (int) l;
		    vehicles[i] = (l%2 == 0) ? TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH) : TestUtilities.factory.createNewSled(Integer.toString(i), Direction.NORTH);
		    vehicles[i].setScheduler(priorityScheduler);
		    vehicles[i].setSpeed(0);
		}

		// Start up car then sleep
		vehicles[0].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Start up sled then give it time to try to enter
		vehicles[1].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Start up car
		vehicles[2].start();
		

		for (Vehicle car: vehicles)
			try {
				car.join();
			} catch (InterruptedException e) {
				fail("Unexpected Interrupt");
			}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;

		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();

			if (currentEvent.getEvent().equals(EventType.ENTER_FAILED)) {

				// Make sure that the second Car never has to wait for the Sled
				if (currentEvent.getVehicle().equals(vehicles[2])) {
					fail("Car 2 was forced to wait when only Car 0 was in the Tunnel");
				}
			}
			
			if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		// make sure all vehicles completed
		for (Vehicle vehicle : vehicles) {
			assertTrue(completed.contains(vehicle), vehicle + " did not complete");
		}
	}
}
