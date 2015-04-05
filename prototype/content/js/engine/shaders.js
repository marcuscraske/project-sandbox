projectSandbox.shaders =
{
	defaultFragment:
		"precision mediump float;" +
		"void main(void) {" +
		"	gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
		"}",
	
	defaultVertex:
		"attribute vec3 aVertexPosition;" +
		"uniform mat4 uMVMatrix;" +
		"uniform mat4 uPMatrix;" +
		"void main(void) {" +
		"	gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);" +
		"}",
		
	createDefaultProgram: function(gl)
	{
		return this.createProgram(gl, this.defaultFragment, this.defaultVertex);
	},
	
	createDefaultTextureProgram: function(gl)
	{
		var fragmentShader = projectSandbox.assetLoader.get("/content/game/shaders/default-texture.frag");
		var vertexShaderSrc = projectSandbox.assetLoader.get("/content/game/shaders/default-texture.vert");
		
		return this.createProgram(gl, fragmentShader, vertexShaderSrc);
	},
		
	createProgram: function(gl, dataFragment, dataVertex)
	{
		// Compile shaders
		var shaderFragment = this.createFragment(gl, dataFragment);
		var shaderVertex = this.createVertex(gl, dataVertex);
		
		// Create program, attach and link
		var shaderProgram = gl.createProgram();
		gl.attachShader(shaderProgram, shaderVertex);
        gl.attachShader(shaderProgram, shaderFragment);
        gl.linkProgram(shaderProgram);
		
		if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
            console.log("Failed to setup shader program.");
        }
		
		// Use the program
		gl.useProgram(shaderProgram);
		
		// Setup vertex position array (?)
		shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);
		
		// Setup vertex texture array
		shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
        gl.enableVertexAttribArray(shaderProgram.textureCoordAttribute);
		
		// Setup uniform matrices for perspective and model-view
		shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
        shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
		shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");
		
		return shaderProgram;
	},
		
	createFragment: function(gl, data)
	{
		return this.create(gl, gl.FRAGMENT_SHADER, data);
	},
	
	createVertex: function(gl, data)
	{
		return this.create(gl, gl.VERTEX_SHADER, data);
	},
	
	create: function(gl, type, data)
	{
		// Create and compile
		var shader = gl.createShader(type);
		gl.shaderSource(shader, data);
		gl.compileShader(shader);
		
		// Check for error
		if(!gl.getShaderParameter(shader, gl.COMPILE_STATUS))
		{
			console.log("Failed to compile shader - error: '" + gl.getShaderInfoLog(shader) + "', data: '" + data + "'");
		}
		
		return shader;
	}
}