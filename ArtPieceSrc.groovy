import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine

import eu.mihosoft.vrl.v3d.*

//def name = "mechEng"
//def name = "boynton"
//def name = "pandemonium"
//def name = "trotting"
//def name = "regatta" // "Worcester's Regatta Roots"
//def name = "regatta_triangles" // triangle study of "Worcester's Regatta Roots"
//def name = "ubiwerks"
//def name = "harrington"
//def name = "stebbins"
//def name = "fightThem" // full title "Fight Them"
//def name = "jankal" // full title "Memories of Kaua'i"
//def name = "toussaint"
//def name = "depose"
//def name = "anmol"
//def name = "wolves" // full title "Wolves At Bay"
//def name = "gigi_tal"
//def name = "solidarityForever"
//def name = "separation"
//def name = "solidarityForever_isabel"
//def name = "horsesDontKnow_union"
//def name = "bisonCouche"
def name = "pumpkin_light_core" // backlit art-nouveau pumpkin panel; standalone, no border/addenda

long tic = System.currentTimeMillis()

ArrayList<Object> desc_params = new ArrayList<Object>()
desc_params.add(name)
ArrayList<Object> borders_params = new ArrayList<Object>()
borders_params.add(name)

def border_width = 4 // width is the offset outward from the painting. so with a 242mm wide painting and 4mm wide border on each side, the resultant piece will be 250mm wide
def border_thickness = 3 // thickness is the z height thickness
def do_rabbet = true
def corner_radius = 6 // mm radius on the four outward corners of a standalone panel; 0 disables

// A standalone panel: no border, no addenda, no base slab -- the painting IS the whole object.
// bread4all was the first such piece and never went through this script; this is the branch for it.
def standalone = (name in ["pumpkin_light_core"])
String local_stl = null // set by the switch below for pieces whose STL is not on the release


println "Clearing the Vitamins cache to make sure current geometry is being used (only run this operation when the STL has changed)"
Vitamins.clear()

String piece_url, release_url, piece_filename
release_url = "https://github.com/JansenSmith/ArtPieceSTLs/releases/download/1.0.0"

println "Loading piece STL from release based on piece name: "+name
switch (name) {
	case "mechEng":
		piece_filename = "WorcesterFreeInstitute_mechEng_Front_250x141.stl"
		break
	case "boynton":
		piece_filename = "WorcesterFreeInstitute_BoyntonHall_Front_250x147.stl"
		break
	case "pandemonium":
		piece_filename = "Pandemonium_Front_236x156.stl"
		break
	case "trotting":
		piece_filename = "trotting_Front_242x139.stl"
		break
	case "regatta":
		piece_filename = "regatta_Front_176x242.stl" // modernized 2026-09-15 export; legacy asset was LakeQuinsigamond1868_Front_176x242.stl
		break
	case "regatta_triangles":
		piece_filename = "regatta_triangles_Front_176x242.stl"
		break
	case "ubiwerks":
		piece_filename = "SteamboatWillie_Front_242x195.stl"
		break
	case "harrington":
		piece_filename = "HarringtonBrothers_Front_242x161.stl"
		break
	case "stebbins":
		piece_filename = "Stebbins_Worcester_Front_242x132.stl"
		break
	case "fightThem":
		piece_filename = "Fight_Front_200x133.stl"
		break
	case "jankal":
		piece_filename = "jankal_kauai_Front_242x182.stl"
		break
	case "toussaint":
		piece_filename = "Toussaint_L.Ouverture_by_George_DeBaptiste_Front_197x242.stl"
		break
	case "depose":
		piece_filename = "depose_Front_229x201.stl"
		break
	case "anmol":
		piece_filename = "anmol_Front_162x242.stl"
		break
	case "wolves":
		piece_filename = "wolves_Front_242x206.stl"
		break
	case "gigi_tal":
		piece_filename = "Gigi_Tal_Front_182x242_v3.stl"
		break
	case "solidarityForever":
	case "solidarityForever_isabel":
		piece_filename = "MayDayMayPole_SolidarityForever_Front_171x242.stl"
		break
	case "separation":
		piece_filename = "separation_fleshBone_Front_242x182.stl"
		break
	case "horsesDontKnow_union":
		piece_filename = "worcester_union_sepia_Front_195x242.stl"
		break
	case "bisonCouche":
		piece_filename = "bisonCouche_Front_242x175.stl"
		break
	case "pumpkin_light_core":
		// local only -- this piece has no release asset
		local_stl = System.getProperty("user.home") + "/Documents/3D-prints/art/pumpkin_light_core_Front_200x200.stl"
		break
	default:
		throw new Exception("Unknown option: $name")
		break
}

if (piece_filename) {
	piece_url = release_url + "/" + piece_filename
}

// Load the .CSG from the disk and cache it in memory
CSG piece
if (local_stl) {
	println "Loading piece STL from local file: "+local_stl
	File localFile = new File(local_stl)
	if (!localFile.exists()) {
		throw new Exception("Local STL not found: "+local_stl)
	}
	piece = Vitamins.get(localFile)
	if (piece == null) {
		throw new Exception("Vitamins.get returned null for local STL: "+local_stl)
	}
} else {
println "Trying to download "+piece_url
try {
	piece = ScriptingEngine.gitScriptRun(
		"https://github.com/JansenSmith/downloadSTL.git", // git location of the library
		"downloadSTL.groovy" , // file to load
		// Parameters passed to the function
		[piece_url]
		)
} catch (Exception e) {
	println "ERROR: downloadSTL factory threw an exception for url='${piece_url}': " + e.getMessage()
	throw e
}
if (piece == null) {
	println "ERROR: downloadSTL factory returned null for url='${piece_url}'. Check the URL is valid and the release asset exists."
	throw new Exception("downloadSTL factory returned null for url='${piece_url}'")
}
}
//println "The original piece STL is "+piece.totalZ+"mm in Z thickness"
println String.format("The original piece STL is %.2fmm in X width, %.2fmm in Y height, %.2fmm in Z thickness", piece.totalX, piece.totalY, piece.totalZ)

switch(name) {
	case "fightThem":
		max_dim = Math.max(piece.totalX,piece.totalY)
		println "Resizing from "+max_dim+"mm to standard 242mm (250mm with standard borders)"
		dim_scale = 242/max_dim
		piece = piece.scalex(dim_scale).scaley(dim_scale)
		break
	default:
		break
}

CSG desc
switch(name) {
	case "pumpkin_light_core": // standalone: no description text
	case "pandemonium":
		break
	default:
		println "Loading description CSG via factory"
		try {
			desc =  (CSG)ScriptingEngine.gitScriptRun(
											"https://github.com/JansenSmith/ArtText.git", // git location of the library
											  "ArtText.groovy" , // file to load
											  desc_params // send the factory the name param
									)
		} catch (Exception e) {
			println "ERROR: ArtText factory threw an exception for name='${name}': " + e.getMessage()
			throw e
		}
		if (desc == null) {
			println "ERROR: ArtText factory returned null for name='${name}'. Check ArtText.groovy handles this name and that the repo is reachable."
			throw new Exception("ArtText factory returned null for name='${name}'")
		}
		break
}

CSG sig
if (!standalone) {
println "Loading signature CSG via factory"
try {
	sig =  (CSG)ScriptingEngine.gitScriptRun(
							"https://github.com/JansenSmith/JMS.git", // git location of the library
							  "JMS.groovy" , // file to load
							  null// no parameters (see next tutorial)
					)
} catch (Exception e) {
	println "ERROR: JMS signature factory threw an exception: " + e.getMessage()
	throw e
}
if (sig == null) {
	println "ERROR: JMS signature factory returned null. Check JMS.groovy is reachable and returns a CSG."
	throw new Exception("JMS signature factory returned null")
}
}


println "Moving piece into position"
piece = piece.toXMin().toYMin().toZMin()

//println "Moving description into position"
switch(name) {
	case "toussaint":
		println "Moving description into position"
		desc = desc.toZMin()
		desc = desc.mirrorx().movex(piece.totalX)
		desc = desc.movey(20)
		break
	case ["regatta", "regatta_triangles"]:
		println "Moving description into position"
		desc = desc.toZMin()
		desc = desc.mirrorx().movex(piece.totalX)
		desc = desc.movey(30) // 20 puts the text into the signature on this piece
		break
	case "pumpkin_light_core": // standalone: nothing to position
	case "pandemonium":
		break
	default:
		println "Moving description into position"
		desc = desc.toZMin()
		desc = desc.mirrorx().movex(piece.totalX)
		break
}


println "Moving signature into position"
switch(name) {
	case "pumpkin_light_core": // standalone: no signature
		break
	case "pandemonium":
		sig = sig.toZMin().movex(piece.totalX).movex(-5)
		break
	default:
		sig = sig.toZMin().movex(piece.totalX)
		sig = sig.mirrorx().movex(piece.totalX)
		break
}


CSG addenda
switch(name) {
	case "pumpkin_light_core": // standalone: no addenda
		addenda = null
		break
	case "pandemonium":
		addenda = sig
		break
	default:
		println "Combine description and signature geometries"
		addenda = sig.union(desc)
		break
}

//println "Creating a base that contains the sig (debug)"
//def solid_space = 0.08
//def base = new Cube(piece.totalX,piece.totalY,addenda.totalZ + solid_space).toCSG()
//				.toXMin().toYMin().toZMin()
//base = base.difference(sig)//.movez(solid_space))
//println "The base is "+base.totalZ+"mm in height"

CSG base = null
if (!standalone) {
println "Creating a base that contains the addenda"
def solid_space = 0.16
switch(name) {
	case "harrington":
		solid_space = 0.32
		break
	case "fightThem":
		solid_space = 0.08
		break
	default:
		break
}
base = new Cube(piece.totalX,piece.totalY,addenda.totalZ + solid_space).toCSG()
				.toXMin().toYMin().toZMin()
base = base.difference(addenda)//.movez(solid_space))
println "The base is "+base.totalZ+"mm in Z thickness"

println "Adding the base to the piece"
piece = piece.dumbUnion(base.toZMax())
				.toZMin()
}

println "Adding borders via factory"
borders_params.add(piece.totalX)
borders_params.add(piece.totalY)
borders_params.add(border_width)
borders_params.add(border_thickness)
borders_params.add(do_rabbet)
CSG borders, backboards
switch(name) {
	case "pumpkin_light_core": // standalone: no border, no rabbet
	case ["mechEng", "boynton"]:
		break
	default:
		println "Loading borders CSG via factory"
		try {
			(borders, backboards) = ScriptingEngine.gitScriptRun(
											"https://github.com/JansenSmith/ArtBorders.git", // git location of the library
											  "ArtBorders.groovy" , // file to load
											  borders_params // send the factory the name param
									) // always returns [border, backboards]; backboards is null when do_rabbet=false
		} catch (Exception e) {
			println "ERROR: ArtBorders factory threw an exception for name='${name}', piece=${piece.totalX}x${piece.totalY}mm, border_width=${border_width}, border_thickness=${border_thickness}: " + e.getMessage()
			throw e
		}
		if (borders == null) {
			println "ERROR: ArtBorders factory returned null for name='${name}', piece=${piece.totalX}x${piece.totalY}mm, border_width=${border_width}, border_thickness=${border_thickness}. Check ArtBorders.groovy and its args handling."
			throw new Exception("ArtBorders factory returned null for name='${name}'")
		}
		piece = piece.dumbUnion(borders)
		addenda = addenda.toXMin(piece).toYMin(piece)
		piece = piece.toXMin().toYMin()
		break
}

//println "Removing description and signature geometries from the piece"
//piece = piece.difference(combin)
//piece = piece.difference(sig)

if (standalone && corner_radius > 0) {
	println "Rounding the panel corners to "+corner_radius+"mm radius"
	double px = piece.totalX, py = piece.totalY, pz = piece.totalZ
	double r = corner_radius
	if (r > px/2 || r > py/2) {
		throw new Exception("corner_radius "+r+"mm exceeds half the panel ("+px+"x"+py+"mm)")
	}
	// One rounded-square tool, then ONE intersect -- the piece mesh is ~2M triangles and
	// every extra boolean against it is a real risk (see bowlerstudio.md on the 5.0.5 engine).
	CSG tool = new Cube(px - 2*r, py, pz*3).toCSG().toXMin().toYMin().toZMin().movex(r)
	tool = tool.union(new Cube(px, py - 2*r, pz*3).toCSG().toXMin().toYMin().toZMin().movey(r))
	for (double cx : [r, px - r]) {
		for (double cy : [r, py - r]) {
			tool = tool.union(new Cylinder(r, r, pz*3, 64).toCSG().toZMin().movex(cx).movey(cy))
		}
	}
	piece = piece.intersect(tool.movez(-pz))
	piece = piece.toXMin().toYMin().toZMin()
}

println String.format("The resultant piece is %.2fmm in X width, %.2fmm in Y height, %.2fmm in Z thickness", piece.totalX, piece.totalY, piece.totalZ)

println "Setting CSG attributes"
piece = piece.setColor(javafx.scene.paint.Color.DARKGRAY)
			.setName(name+"_piece")
			.addAssemblyStep(0, new Transform())
			.setManufacturing({ toMfg ->
				return toMfg
						//.rotx(180)// fix the orientation
						//.toZMin()//move it down to the flat surface
			})

if (desc) {
	desc = desc.setColor(javafx.scene.paint.Color.DARKRED)
				.setName(name+"_desc")
				.addAssemblyStep(0, new Transform())
				.setManufacturing({ toMfg ->
					return toMfg
							//.rotx(180)// fix the orientation
							//.toZMin()//move it down to the flat surface
				})
}

if (sig) {
sig = sig.setColor(javafx.scene.paint.Color.DARKRED)
			.setName(name+"_sig")
			.addAssemblyStep(0, new Transform())
			.setManufacturing({ toMfg ->
				return toMfg
						//.rotx(180)// fix the orientation
						//.toZMin()//move it down to the flat surface
			})

}

if (addenda) {
addenda = addenda.setColor(javafx.scene.paint.Color.DARKRED)
			.setName(name+"_addenda")
			.addAssemblyStep(0, new Transform())
			.setManufacturing({ toMfg ->
				return toMfg
						//.rotx(180)// fix the orientation
						//.toZMin()//move it down to the flat surface
			})
			
}

if (base) {
base = base.setColor(javafx.scene.paint.Color.DARKGRAY)
			.setName(name+"_base")
			.addAssemblyStep(0, new Transform())
			.setManufacturing({ toMfg ->
				return toMfg
						//.rotx(180)// fix the orientation
						//.toZMin()//move it down to the flat surface
			})

}

def ret = standalone ? [piece] : (backboards ? [piece, addenda, backboards] : [piece, addenda])

println "Exporting manufacturing files"
File outDir = new File(System.getProperty("user.home") + "/Documents/3D-prints/art/" + name)
outDir.mkdirs()
new CadFileExporter().generateManufacturingParts(ret, outDir)
println "Exported to " + outDir.getAbsolutePath()

println "Done!"

long toc = System.currentTimeMillis()
println "Total elapsed: ${(toc - tic).intdiv(1000)}s"

return ret

