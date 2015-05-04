projectSandbox.inventory =
{
	// Array of slot IDs to retain order in which to render items
	renderOrder: new Array(),

	// K (slotid), V (item)
	items: new Map(),

	// Slot ID selected
	selectedSlotId: -1,

	selectPrevKeyDown: false,
	selectNextKeyDown: false,


	reset: function()
	{
	    this.items = new Map();
	    this.renderOrder = new Array();

	    console.debug("Inventory - reset");
	},

	logic: function()
	{
	    // Check if to switch items
	    var q = projectSandbox.keyboard.Q;
	    var e = projectSandbox.keyboard.E;

	    // -- Previous
	    if (!this.selectPrevKeyDown && q)
	    {
	        this.selectPrevKeyDown = true;
	        this.selectItemPrevious();
	    }
	    else if (this.selectPrevKeyDown && !q)
	    {
	        this.selectPrevKeyDown = false;
	    }

	    // -- Next
        if (!this.selectNextKeyDown && e)
        {
            this.selectNextKeyDown = true;
            this.selectItemNext();
        }
        else if (this.selectNextKeyDown && !e)
        {
            this.selectNextKeyDown = false;
        }

	},

    findItemIndex: function(slotId)
    {
        for (var i = 0; i < this.renderOrder.length; i++)
        {
            if (this.renderOrder[i] == slotId)
            {
                return i;
            }
        }

        return null;
    },

	selectItemPrevious: function()
	{
        this.selectItem(-1);
	},

	selectItemNext: function()
	{
	    this.selectItem(1);
	},

	selectItem: function(indexOffset)
	{
	    var index = this.findItemIndex(this.selectedSlotId);

        if (index != null)
        {
            var newIndex = index + indexOffset;
            var renderOrderLen = this.renderOrder.length;

            if (newIndex < 0)
            {
                newIndex = renderOrderLen + newIndex;
            }
            else if (newIndex >= renderOrderLen)
            {
                newIndex = newIndex - renderOrderLen;
            }

            var newSlotId = this.renderOrder[newIndex];

            // Build packet to send to server
            var buff = new Uint8Array(3);
            var dv = new DataView(buff.buffer);

            buff[0] = "I".charCodeAt(0);
            buff[1] = "S".charCodeAt(0);
            dv.setInt8(2, newSlotId);

            // Send packet to server
            projectSandbox.comms.send(buff.buffer);
        }
	},

	packetInventory: function(subType, data)
	{
		switch (subType)
		{
			case 'U': // Updates
			    console.debug("Inventory - updates packet");
				this.packetInventoryUpdates(data);
				return;
			default:
				console.error("Inventory - unhandled packet - " + subType);
				break;
		}
	},

	packetInventoryUpdates: function(data)
	{
		var dataView = new DataView(data.buffer);

		var offset = 2; // maintype/subtype

		var updateType;

		while (offset < data.length)
		{
			// Read the type of update
			updateType = String.fromCharCode(dataView.getInt8(offset));
			offset += 1;

			// Handle the rest of the data based on the type
			switch (updateType)
			{
				case "R":
					offset = this.packetInventoryReset(data, dataView, offset);
					break;
				case "S":
					offset = this.packetInventoryItemSelected(data, dataView, offset);
					break;
				case "N":
					offset = this.packetInventoryItemNonSelected(data, dataView, offset);
					break;
				case "C":
					offset = this.packetInventoryItemCreated(data, dataView, offset);
					break;
				case "R":
					offset = this.packetInventoryItemRemoved(data, dataView, offset);
					break;
				case "M":
					offset = this.packetInventoryItemChanged(data, dataView, offset);
					break;
                default:
                    console.error("Inventory - unhandled update type - " + updateType);
                    break;
			}
		}
	},

    packetInventoryReset: function(data, dataView, offset)
    {
        this.reset();

        return offset;
    },

	packetInventoryItemSelected: function(data, dataView, offset)
	{
		this.selectedSlotId = dataView.getInt8(offset);

		console.debug("Inventory - item selected - " + this.selectedSlotId);

		// Call UI hook
		projectSandbox.ui.hookInventory_selectedChanged();

		return offset + 1;
	},

	packetInventoryItemNonSelected: function(data, dataView, offset)
	{
		this.selected = -1;

		console.debug("Inventory - no item selected");

		return offset;
	},

	packetInventoryItemCreated: function(data, dataView, offset)
	{
		// Parse mandatory data
		var slotId = dataView.getInt8(offset);
		offset += 1;

		var typeId = dataView.getInt16(offset);
		offset += 2;

		// Check it doesn't already exist
		if (this.items.has(slotId))
		{
			console.error("Unable to add duplicate slot to inventory - " + slotId);
			return;
		}

		// Create type
		var item;

		switch (typeId)
		{
		    // Fist
		    case 1:
		        item = new Fist(slotId);
		        console.debug("Inventory - created fist - " + slotId);
		        break;
			// Weapons -> SMG
			case 100:
				item = new Smg(slotId);
				console.debug("Inventory - created SMG - " + slotId);
				break;
			default:
				console.error("Inventory - cannot create item - no type exists - " + typeId);
				return offset;
		}

		// Allow item to read custom data
		offset = item.packetCreate(data, dataView, offset);

		// Add to inventory
		this.items.set(slotId, item);
		this.renderOrder.push(slotId);

		return offset;
	},

	packetInventoryItemRemoved: function(data, dataView, offset)
	{
		// Parse mandatory data
		var slotId = dataView.getInt8(offset);
		offset += 1;

		// Fetch item
		var item = this.items.get(slotId);

        if (item != null)
        {
            // Allow item to read custom data
            offset = item.packetRemove(data, dataView, offset);

            // Remove from collections
            this.items.delete(slotId);
            this.renderOrder.splice(item, 1);

            console.debug("Inventory - removed item - " + slotId);
		}
		else
		{
		    console.error("Inventory - attempted to remove missing item: " + slotId);
		}

		return offset;
	},

	packetInventoryItemChanged: function(data, dataView, offset)
	{
		// Parse mandatory data
		var slotId = dataView.getInt8(offset);
		offset += 1;

		// Fetch item
		var item = this.items.get(slotId);

        if (item != null)
        {
		    // Allow item to read custom data
            offset = item.packetChanged(data, dataView, offset);

            console.debug("Inventory - item changed - " + slotId);
		}
		else
		{
		    console.error("Inventory - change occurred for missing item: " + slotId);
		}

		return offset;
	},

	getSelected: function()
	{
	    if (this.selectedSlotId == -1)
	    {
	        return null;
	    }

	    return this.items.get(this.selectedSlotId);
	}
}
