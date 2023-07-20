package edu.brandeis.cs.cs131.pa3;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import edu.brandeis.cs.cs131.pa3.logging.Event;
import edu.brandeis.cs.cs131.pa3.logging.EventType;
import edu.brandeis.cs.cs131.pa3.tunnel.Direction;
import edu.brandeis.cs.cs131.pa3.tunnel.Tunnel;
import edu.brandeis.cs.cs131.pa3.tunnel.Vehicle;

public class BehaviorTest {

	@BeforeEach
	public void setUp() {
		Tunnel.DEFAULT_LOG.clearLog();
	}

	@BeforeAll
	public static void broadcast() {
		System.out.printf("Running Behavior Tests using %s \n", TestUtilities.factory.getClass().getCanonicalName());
	}

	private static final int NUM_RUNS = 10;

	/**
	 * Vehicle RollCall checks the basic functions of a vehicle. Note if the test
	 * does not pass neither will any other test *
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void VehicleRollCall() {

		for (Direction direction : Direction.values()) {
			Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], direction);
			Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[1], direction);

			assertTrue(car.getDirection().equals(direction), "car is the wrong direction");
			assertTrue(sled.getDirection().equals(direction), "sled is the wrong direction");

			assertEquals(car.getVehicleName(), TestUtilities.gbNames[0]);
			assertEquals(sled.getVehicleName(), TestUtilities.gbNames[1]);

			assertEquals(String.format("%s %s %s", direction, TestUtilities.carName, TestUtilities.gbNames[0]),
					(car.toString()));
			assertEquals(String.format("%s %s %s", direction, TestUtilities.sledName, TestUtilities.gbNames[1]),
					(sled.toString()));

		}
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Tunnel_Basic() {
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
		assertTrue(TestUtilities.mrNames[0].equals(tunnel.getName()), "Tunnel has the wrong name");
		assertTrue(String.format("%s", TestUtilities.mrNames[0]).equals(tunnel.toString()),
				"Tunnel toString does not function as expected");
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void car_Enter() {
		Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.random());
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(car, tunnel);
		Event logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(car, tunnel, EventType.ENTER_ATTEMPT).weakEquals(logEvent),
				"Tunnel log did not record vehicle entering tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(car, tunnel, EventType.ENTER_SUCCESS).weakEquals(logEvent),
				"Tunnel log did not record vehicle entering tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(car, tunnel, EventType.LEAVE_START).weakEquals(logEvent),
				"Tunnel log did not record vehicle leaving tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(car, tunnel, EventType.LEAVE_END).weakEquals(logEvent),
				"Tunnel log did not record vehicle leaving tunnel");
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void sled_Enter() {
		Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[0], Direction.random());
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(sled, tunnel);
		Event logEvent = Tunnel.DEFAULT_LOG.get();

		assertTrue(new Event(sled, tunnel, EventType.ENTER_ATTEMPT).weakEquals(logEvent),
				"Tunnel log did not record sled entering tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(sled, tunnel, EventType.ENTER_SUCCESS).weakEquals(logEvent),
				"Tunnel log did not record sled entering tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(sled, tunnel, EventType.LEAVE_START).weakEquals(logEvent),
				"Tunnel log did not record sled entering tunnel");
		logEvent = Tunnel.DEFAULT_LOG.get();
		assertTrue(new Event(sled, tunnel, EventType.LEAVE_END).weakEquals(logEvent),
				"Tunnel log did not record sled entering tunnel");
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Direction_Constraint() {
		Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
		Vehicle violator = TestUtilities.factory.createNewCar(TestUtilities.gbNames[1], Direction.SOUTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
		boolean canUse = tunnel.tryToEnter(car);
		assertTrue(canUse, String.format("%s cannot use", car));
		canUse = tunnel.tryToEnter(violator);
		assertTrue(!canUse, String.format("%s is using with %s. Violates industry constraint", violator, car));
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Multiple_cars() {
		Vehicle car1 = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
		Vehicle car2 = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
		Vehicle car3 = TestUtilities.factory.createNewCar(TestUtilities.gbNames[1], Direction.NORTH);
		Vehicle car4 = TestUtilities.factory.createNewCar(TestUtilities.gbNames[7], Direction.NORTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
		boolean canUse = tunnel.tryToEnter(car2);
		assertTrue(canUse, String.format("%s cannot use", car2));
		canUse = tunnel.tryToEnter(car3);
		assertTrue(canUse, String.format("%s is not using with %s.", car2, car3));
		canUse = tunnel.tryToEnter(car1);
		assertTrue(canUse, String.format("%s is not using with %s and %s.", car1, car2, car3));
		canUse = tunnel.tryToEnter(car4);
		assertTrue(!canUse,
				String.format("%s is using with %s, %s and %s violates number constraint.", car4, car2, car3, car1));
		car2.doWhileInTunnel();
		tunnel.exitTunnel(car2);
		car3.doWhileInTunnel();
		tunnel.exitTunnel(car3);
		canUse = tunnel.tryToEnter(car4);
		assertTrue(canUse, String.format("%s cannot use, %s and %s did not leave tunnel.", car4, car2, car3));
	}

	/**
	 * Makes sure that a Car and Sled cannot be entered into the same Tunnel.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarAndSled() {
		// Create car, sled both going north
		Vehicle car = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		Vehicle sled = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Adding the car should be ok, but shouldn't be able to add sled
		assertTrue(tunnel.tryToEnter(car), car + " did not enter");
		assertFalse(tunnel.tryToEnter(sled), sled + " entered when " + car + " was there");

		// Make a new empty Tunnel
		tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Adding the sled should be ok, but shouldn't be able to add car
		assertTrue(tunnel.tryToEnter(sled), sled + " did not enter");
		assertFalse(tunnel.tryToEnter(car), car + " entered when " + sled + " was there");
	}

	/**
	 * Makes sure that more than one Sled cannot be entered into the same Tunnel.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testSledNumberConstraint() {
		// Make 2 sleds both going north
		Vehicle sled1 = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		Vehicle sled2 = TestUtilities.factory.createNewSled("1", Direction.NORTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Adding 1 sled should be ok, but second should not enter
		assertTrue(tunnel.tryToEnter(sled1), sled1 + " did not enter");
		assertFalse(tunnel.tryToEnter(sled2), sled2 + " entered when " + sled1 + " was there");
	}

	/**
	 * Makes sure that Vehicles going in a new direction can enter an emptied
	 * Tunnel.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testNewDirectionAfterTunnelEmptied() {
		// Set up 1 car going north, 1 car going south; 1 sled going north, 1 sled going
		// south and a Tunnel
		Vehicle carNorth = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		Vehicle carSouth = TestUtilities.factory.createNewCar("0", Direction.SOUTH);
		Vehicle sledNorth = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		Vehicle sledSouth = TestUtilities.factory.createNewSled("0", Direction.SOUTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Enter and then exit the north car
		assertTrue(tunnel.tryToEnter(carNorth), carNorth + " should have entered");
		tunnel.exitTunnel(carNorth);

		// Should now allow for a south car to enter
		assertTrue(tunnel.tryToEnter(carSouth), carSouth + " should have entered");

		// Reset Tunnel to start next test
		tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Enter and then exit the north sled
		assertTrue(tunnel.tryToEnter(sledNorth), carNorth + " should have entered");
		tunnel.exitTunnel(sledNorth);

		// Should now allow for a south sled to enter
		assertTrue(tunnel.tryToEnter(sledSouth), carSouth + " should have entered");
	}

	/**
	 * Makes sure that a new Vehicle type can enter an emptied Tunnel.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testNewVehicleAfterTunnelEmptied() {
		// Set up Car, Sled both going North as well as a Tunnel
		Vehicle car = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		Vehicle sled = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Should be able to enter the car, then the sled after car is removed
		assertTrue(tunnel.tryToEnter(car));
		tunnel.exitTunnel(car);
		assertTrue(tunnel.tryToEnter(sled));

		// Reset tunnel to start next test
		tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);

		// Should be able to enter the sled, then the car after sled is removed
		assertTrue(tunnel.tryToEnter(sled));
		tunnel.exitTunnel(sled);
		assertTrue(tunnel.tryToEnter(car));
	}
}
