game.ui =
{
	// Size of the UI viewport
	uiWidth: 800,
	uiHeight: 600,
	
	primitiveTest: null,
	
	// Weapon icon
	iconWeapon: null,
	
	// Bounty / wanted stars
	starsIcon: null,
	starsOn: true,
	
	setup: function()
	{
		this.rebuildUI();
	},
	
	resize: function()
	{
		// TODO: call this.
	},
	
	rebuildUI: function()
	{
		var gl = projectSandbox.gl;
		var width = gl.viewportWidth;
		var height = gl.viewportHeight;
		var ratio = width / height;
		
		// Create UI primitives
		this.primitiveTest = new Primitive(128, 128);
		this.primitiveTest.setTexture("vehicles/ice-cream-van");
		
		// Create weapons icon
		this.iconWeapon = new Primitive(48 * ratio, 48 * ratio);
		this.iconWeapon.setTexture("ui-weapons/fist");
		this.setPrimitivePosTopLeft(this.iconWeapon, width, height, 8, 8);
		
		// Set initial stars
		var starCount = 6;
		
		this.starsIcon = [];
		var star;
		for (var i = 0; i < starCount; i++)
		{
			star = new Primitive(16 * ratio, 16 * ratio);
			star.setTexture("ui/star_off");
			this.setPrimitivePosTopLeft(star, width, height, 8 + ((star.width) * i), 75);
			
			this.starsIcon[i] = star;
		}
	},
	
	setPrimitivePosTopLeft: function(primitive, viewWidth, viewHeight, x, y)
	{
		primitive.x = viewWidth - x - (primitive.width / 2.0);
		primitive.y = viewHeight - y - (primitive.height / 2.0);
	},

	reset: function()
	{
		// Does nothing at present...
	},
	
	render: function(gl, shaderProgram, modelView, perspective)
	{
		var width = gl.viewportWidth;
		var height = gl.viewportHeight;
		var ratio = width / height;
		
		// Switch into orthographic mode
		mat4.ortho(perspective, 0, this.uiWidth, 0, this.uiHeight, 0, 1);
		mat4.identity(modelView);
		
		// Render cash score
		this.primitiveTest.render(gl, shaderProgram, modelView, perspective);
		
		// Render health bar
		
		// Render weapon icon
		this.iconWeapon.render(gl, shaderProgram, modelView, perspective);
		
		// Render stars / bounty level
		if (this.starsOn)
		{
			for(var i = 0; i < this.starsIcon.length; i++)
			{
				this.starsIcon[i].render(gl, shaderProgram, modelView, perspective);
			}
		}
	}
}
