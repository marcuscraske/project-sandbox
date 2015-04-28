projectSandbox.commsPacket =
{
	/*
		The first two bytes indicate the type of packet.
		
		Packet types:
			title				b0		b1		target		bs	len		description
			----------------------------------------------------------------------------------
			update movement		U		M		S			3	2		entity id
															5	1		movement flags
			
			list of ents		L		E		C			3 	2		entity id
															5	8		position x
															13	8		position y
															21	8		rotation
															
			the ply's entid		U		I		C			3	2		entity id
										
	*/

	// Movement constants
	MOVEMENT_UP: 1,
	MOVEMENT_LEFT: 2,
	MOVEMENT_DOWN: 4,
	MOVEMENT_RIGHT: 8,
	
	ACTION_KEY: 16,
	
	NUMBER1: 32,
	NUMBER2: 64,
	NUMBER3: 128,
	NUMBER4: 256,
	NUMBER5: 512,
	NUMBER6: 1024,
	NUMBER7: 2048,
	NUMBER8: 4096,
	NUMBER9: 8192,
	NUMBER0: 16384,
	
	SPACEBAR: 32768,
	
	// Previous packet - no point updating the server if the value is the same!
	previousMovement: 0,
	
	sendSessionId: function()
	{
		var buff = new Uint8Array(38);
		
		// Header data
		buff[0] = "U".charCodeAt(0); // U
		buff[1] = "S".charCodeAt(0); // S
		
		// Session ID / UUID
		for(var i = 0; i < 36; i++)
		{
			buff[2+i] = projectSandbox.sessionId.charCodeAt(i);
		}
		
		// Send packet
		projectSandbox.comms.send(buff.buffer);
	},
	
	updateMovement: function()
	{
		// Compute new movement packet
		var movement = 0;
		
		if(projectSandbox.keyboard.W)
		{
			movement |= this.MOVEMENT_UP;
		}
		if(projectSandbox.keyboard.S)
		{
			movement |= this.MOVEMENT_DOWN;
		}
		if(projectSandbox.keyboard.A)
		{
			movement |= this.MOVEMENT_LEFT;
		}
		if(projectSandbox.keyboard.D)
		{
			movement |= this.MOVEMENT_RIGHT;
		}
		
		if (projectSandbox.keyboard.E)
		{
			movement |= this.ACTION_KEY;
		}
		
		if (projectSandbox.keyboard.NUMBER1)
		{
			movement |= this.NUMBER1;
		}
		if (projectSandbox.keyboard.NUMBER2)
		{
			movement |= this.NUMBER2;
		}
		if (projectSandbox.keyboard.NUMBER3)
		{
			movement |= this.NUMBER3;
		}
		if (projectSandbox.keyboard.NUMBER4)
		{
			movement |= this.NUMBER4;
		}
		if (projectSandbox.keyboard.NUMBER5)
		{
			movement |= this.NUMBER5;
		}
		if (projectSandbox.keyboard.NUMBER6)
		{
			movement |= this.NUMBER6;
		}
		if (projectSandbox.keyboard.NUMBER7)
		{
			movement |= this.NUMBER7;
		}
		if (projectSandbox.keyboard.NUMBER8)
		{
			movement |= this.NUMBER8;
		}
		if (projectSandbox.keyboard.NUMBER9)
		{
			movement |= this.NUMBER9;
		}
		if (projectSandbox.keyboard.NUMBER0)
		{
			movement |= this.NUMBER0;
		}
		
		if (projectSandbox.keyboard.SPACEBAR)
		{
			movement |= this.SPACEBAR;
		}
		
		// Compare and decide if to send
		if(movement != this.previousMovement)
		{
			// Update state
			this.previousMovement = movement;
			
			// Build packet
			var buff = new Uint8Array(6);
			var dv = new DataView(buff.buffer);
			// -- Header data
			buff[0] = 85; // U
			buff[1] = 77; // M
			// -- Entity ID
			dv.setInt16(2, projectSandbox.playerEntityId);
			// -- Movement flags
			dv.setInt16(4, movement);
			
			// Send packet
			projectSandbox.comms.send(buff.buffer);
		}
	}
	
}