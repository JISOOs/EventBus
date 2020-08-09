import java.util.Random;

import EventPackage.Event;
import EventPackage.EventManagerInterface;
import EventPackage.EventQueue;
import InstrumentationPackage.MessageWindow;

public class DoorSensor {
	public static void main(String args[]) {
		String EvtMgrIP; // Event Manager IP address
		Event Evt = null; // Event object
		EventQueue eq = null; // Message Queue
		int EvtId = 0; // User specified event ID
		EventManagerInterface em = null;// Interface object to the event manager
		boolean DoorState = false; // windows state: false == 정상, true == 비정상
		float CurrentDoor;

		int Delay = 2500; // The loop delay (2.5 seconds)
		boolean Done = false; // Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

		if (args.length == 0) {
			// event manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine...");

			try {
				// Here we create an event manager interface object. This assumes
				// that the event manager is on the local machine

				em = new EventManagerInterface();
			}

			catch (Exception e) {
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} else {

			// event manager is not on the local system

			EvtMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + EvtMgrIP);

			try {
				// Here we create an event manager interface object. This assumes
				// that the event manager is NOT on the local machine

				em = new EventManagerInterface(EvtMgrIP);
			}

			catch (Exception e) {
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (em != null) {

			// We create a message window. Note that we place this panel about 1/2 across
			// and 1/3 down the screen

			float WinPosX = 0.5f; // This is the X position of the message window in terms
									// of a percentage of the screen height
			float WinPosY = 0.4f; // This is the Y position of the message window in terms
									// of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Door Sensor", WinPosX, WinPosY);

			mw.WriteMessage("Registered with the event manager.");

			try {
				mw.WriteMessage("   Participant id: " + em.GetMyId());
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

			} // try

			catch (Exception e) {
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("\nInitializing Windows Simulation:: ");
			CurrentDoor = GetRandomNumber() * (float) 100.00;

			mw.WriteMessage("\nInitial Windows Simulation Set::" + CurrentDoor);

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");

			while (!Done) {
				// Post the current temperature

				PostDoor(em, CurrentDoor);

				mw.WriteMessage("Current Window::  " + CurrentDoor + " ");

				// Get the message queue

				try {
					eq = em.GetEventQueue();

				} // try

				catch (Exception e) {
					mw.WriteMessage("Error getting event queue::" + e);

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = -5, this means the the heater
				// or chiller has been turned on/off. Note that we get all the messages
				// at once... there is a 2.5 second delay between samples,.. so
				// the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the temperature as it would in reality.

				int qlen = eq.GetSize();

				for (int i = 0; i < qlen; i++) {
					Evt = eq.GetEvent();

					if (Evt.GetEventId() == -2) {
						if (Evt.GetMessage().equalsIgnoreCase("D1")) // alarm on
						{
							DoorState = true;

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("D0")) // alarm off
						{
							DoorState = false;

						} // if

					} // if

					// If the event ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the event manager.

					if (Evt.GetEventId() == 99) {
						Done = true;

						try {
							em.UnRegister();

						} // try

						catch (Exception e) {
							mw.WriteMessage("Error unregistering: " + e);

						} // catch

						mw.WriteMessage("\n\nSimulation Stopped. \n");

					} // if

				} // for

				// Now we trend the temperature according to the status of the
				// heater/chiller controller.

				if (DoorState) {
					CurrentDoor += GetRandomNumber();

				}

				// Here we wait for a 2.5 seconds before we start the next sample

				try {
					Thread.sleep(Delay);

				} // try

				catch (Exception e) {
					mw.WriteMessage("Sleep error:: " + e);

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the event manager.\n\n");

		} // if

	} // main

	/***************************************************************************
	 * CONCRETE METHOD:: GetRandomNumber Purpose: This method provides the
	 * simulation with random floating point temperature values between 0.1 and 0.9.
	 *
	 * Arguments: None.
	 *
	 * Returns: float
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private float GetRandomNumber() {
		Random r = new Random();
		Float Val;

		Val = Float.valueOf((float) -1.0);

		while (Val < 0.1) {
			Val = r.nextFloat();
		}

		return (Val.floatValue());

	} // GetRandomNumber

	/***************************************************************************
	 * CONCRETE METHOD:: PostTemperature Purpose: This method posts the specified
	 * temperature value to the specified event manager. This method assumes an
	 * event ID of 1.
	 *
	 * Arguments: EventManagerInterface ei - this is the eventmanger interface where
	 * the event will be posted.
	 *
	 * float temperature - this is the temp value.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void PostDoor(EventManagerInterface ei, float door) {
		// Here we create the event.

		Event evt = new Event((int) 4, String.valueOf(door));

		// Here we send the event to the event manager.

		try {
			ei.SendEvent(evt);
			// System.out.println( "Sent Temp Event" );

		} // try

		catch (Exception e) {
			System.out.println("Error Posting Door:: " + e);

		} // catch

	} // PostTemperature
}// WindowSensor
