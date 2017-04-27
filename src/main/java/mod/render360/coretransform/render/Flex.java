package mod.render360.coretransform.render;

import java.util.List;

import mod.render360.coretransform.gui.Slider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class Flex extends RenderMethod {

	// Auto-generated by update-shader.sh
  private final String fragmentShader = "// NOTE: run update-shader.sh to dump this file into the appropriate Java string\n// for compilation.\n\n#version 130\n\n#define M_PI 3.14159265\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\n\nuniform vec2 pixelOffset[4];\n\n//fovx\nuniform float fovx;\nuniform float aspect;\n\nuniform vec2 cursorPos;\n\nuniform bool drawCursor;\n\nuniform vec4 backgroundColor;\n\nout vec4 color;\n\nvec2 tex_to_lens(vec2 tex) {\n  return (tex - vec2(0.5, 0.5)) * vec2(2,2/aspect);\n}\n\nvec3 latlon_to_ray(float lat, float lon) {\n  return vec3(\n    sin(lon)*cos(lat),\n    sin(lat),\n    cos(lon)*cos(lat)\n  );\n}\n\nvec3 standard_inverse(vec2 lenscoord) {\n  float x = lenscoord.x;\n  float y = lenscoord.y;\n  float r = length(lenscoord);\n  float theta = atan(r);\n  float s = sin(theta);\n  return vec3(x/r*s, y/r*s, cos(theta));\n}\nvec2 standard_forward(float lat, float lon) {\n  vec3 ray = latlon_to_ray(lat, lon);\n  float x = ray.x;\n  float y = ray.y;\n  float z = ray.z;\n  float theta = acos(z);\n  float r = tan(theta);\n  float c = r/length(ray.xy);\n  return vec2(x*c, y*c);\n}\nvec3 standard_ray(vec2 lenscoord) {\n  float scale = standard_forward(0, radians(fovx)/2).x;\n  return standard_inverse(lenscoord * scale);\n}\n\nvec3 panini_inverse(vec2 lenscoord) {\n  float x = lenscoord.x;\n  float y = lenscoord.y;\n  float d = 1;\n  float k = x*x/((d+1)*(d+1));\n  float dscr = k*k*d*d - (k+1)*(k*d*d-1);\n  float clon = (-k*d+sqrt(dscr))/(k+1);\n  float S = (d+1)/(d+clon);\n  float lon = atan(x,S*clon);\n  float lat = atan(y,S);\n  return latlon_to_ray(lat, lon);\n}\nvec2 panini_forward(float lat, float lon) {\n  float d = 1;\n  float S = (d+1)/(d+cos(lon));\n  float x = S*sin(lon);\n  float y = S*tan(lat);\n  return vec2(x,y);\n}\nvec3 panini_ray(vec2 lenscoord) {\n  float scale = panini_forward(0, radians(fovx)/2).x;\n  return panini_inverse(lenscoord * scale);\n}\n\nvec3 mercator_inverse(vec2 lenscoord) {\n  float lon = lenscoord.x;\n  float lat = atan(sinh(lenscoord.y));\n  return latlon_to_ray(lat, lon);\n}\nvec2 mercator_forward(float lat, float lon) {\n  float x = lon;\n  float y = log(tan(M_PI*0.25+lat*0.5));\n  return vec2(x,y);\n}\nvec3 mercator_ray(vec2 lenscoord) {\n  float scale = mercator_forward(0, radians(fovx)/2).x;\n  return mercator_inverse(lenscoord * scale);\n}\n\nvec3 equirect_inverse(vec2 lenscoord) {\n  if (abs(lenscoord.x) > M_PI || abs(lenscoord.y) > M_PI/2) {\n    return vec3(0,0,0);\n  }\n  float lon = lenscoord.x;\n  float lat = lenscoord.y;\n  return latlon_to_ray(lat, lon);\n}\nvec2 equirect_forward(float lat, float lon) {\n  float x = lon;\n  float y = lat;\n  return vec2(x,y);\n}\nvec3 equirect_ray(vec2 lenscoord) {\n  float scale = equirect_forward(0, radians(fovx)/2).x;\n  return equirect_inverse(lenscoord * scale);\n}\n\nvec3 stereographic_inverse(vec2 lenscoord) {\n  float x = lenscoord.x;\n  float y = lenscoord.y;\n  float angleScale = 0.5;\n  float r = length(lenscoord);\n  float theta = atan(r)/angleScale;\n  float s = sin(theta);\n  return vec3(x/r*s, y/r*s, cos(theta));\n}\nvec2 stereographic_forward(float lat, float lon) {\n  vec3 ray = latlon_to_ray(lat, lon);\n  float theta = acos(ray.z);\n  float angleScale = 0.5;\n  float r = tan(theta*angleScale);\n  float c = r/length(ray.xy);\n  return vec2(ray.x*c, ray.y*c);\n}\nvec3 stereographic_ray(vec2 lenscoord) {\n  float scale = stereographic_forward(0, radians(fovx)/2).x;\n  return stereographic_inverse(lenscoord * scale);\n}\n\nvoid main(void) {\n  /* Ray-trace a cube */\n\n	//Anti-aliasing\n	vec4 colorN[4];\n\n	for (int loop = 0; loop < 4; loop++) {\n\n    float alpha = 1;\n\n		//create ray\n		vec2 c = tex_to_lens(texcoord);\n    vec3 ray;\n    if (fovx < 120) {\n      ray = standard_ray(c);\n    } else if (fovx < 140) {\n      ray = mix(standard_ray(c), stereographic_ray(c), (fovx - 120)/ 20.0);\n    } else if (fovx < 200) {\n      ray = stereographic_ray(c);\n    } else if (fovx < 220) {\n      ray = mix(stereographic_ray(c), mercator_ray(c), (fovx - 200)/ 20.0);\n    } else if (fovx < 340) {\n      ray = mercator_ray(c);\n    } else if (fovx < 360) {\n      ray = mix(mercator_ray(c), equirect_ray(c), (fovx - 340)/ 20.0);\n      float len = length(ray);\n      alpha = clamp(len*2, 0, 1);\n    } else if (fovx == 360) {\n      ray = equirect_ray(c);\n    } else {\n      ray = vec3(0,0,0);\n    }\n    ray.z *= -1;\n\n    if (length(ray) == 0) {\n      colorN[loop] = backgroundColor;\n      continue;\n    }\n\n		//find which side to use\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n\n    colorN[loop] = mix(vec4(0,0,0,1), colorN[loop], alpha);\n\n		if (drawCursor) {\n			vec2 normalAngle = cursorPos*2 - 1;\n			float x = ray.x / -ray.z;\n			float y = ray.y / -ray.z;\n			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&\n				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&\n				ray.z < 0) {\n				colorN[loop] = vec4(1, 1, 1, 1);\n			}\n		}\n	}\n\n	color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n}\n";

	private float fov = 180;

	@Override
	public String getName() {
		return "Flex";
	}

	@Override
	public String getFragmentShader() {
		return this.fragmentShader;
	}

	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}

	@Override
	public void addButtonsToGui(List<GuiButton> buttonList, int width, int height) {
		super.addButtonsToGui(buttonList, width, height);
		buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, fov, 1f, null));
	}

	@Override
	public float getFOV() {
		return fov;
	}

	public class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {

		}

		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			if (id == 18104) {
				fov = value;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {

		}
	}
}
