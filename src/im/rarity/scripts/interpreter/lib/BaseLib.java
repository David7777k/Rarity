package im.rarity.scripts.interpreter.lib;

import java.io.InputStream;

import im.rarity.scripts.interpreter.Globals;
import im.rarity.scripts.interpreter.LuaError;
import im.rarity.scripts.interpreter.LuaString;
import im.rarity.scripts.interpreter.LuaValue;
import im.rarity.scripts.interpreter.Varargs;


public class BaseLib extends TwoArgFunction implements ResourceFinder {

	Globals globals;
	public LuaValue call(LuaValue modname, LuaValue env) {
		globals = env.checkglobals();
		globals.finder = this;
		globals.baselib = this;
		env.set("print", new print(this));
		env.set("tonumber", new tonumber());
		env.set("tostring", new tostring());
		env.set("error", new error());
		return env;
	}


	public InputStream findResource(String filename) {
		return getClass().getResourceAsStream(filename.startsWith("/")? filename: "/"+filename);
	}



	static final class _assert extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			if ( !args.arg1().toboolean() )
				error( args.narg()>1? args.optjstring(2,"assertion failed!"): "assertion failed!" );
			return args;
		}
	}


	static final class collectgarbage extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			String s = args.optjstring(1, "collect");
			if ( "collect".equals(s) ) {
				System.gc();
				return ZERO;
			} else if ( "count".equals(s) ) {
				Runtime rt = Runtime.getRuntime();
				long used = rt.totalMemory() - rt.freeMemory();
				return varargsOf(valueOf(used/1024.), valueOf(used%1024));
			} else if ( "step".equals(s) ) {
				System.gc();
				return LuaValue.TRUE;
			} else {
				argerror(1, "invalid option '" + s + "'");
			}
			return NIL;
		}
	}

	static final class error extends TwoArgFunction {
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			if (arg1.isnil()) throw new LuaError(NIL);
			if (!arg1.isstring() || arg2.optint(1) == 0) throw new LuaError(arg1);
			throw new LuaError(arg1.tojstring(), arg2.optint(1));
		}
	}





	final class print extends VarArgFunction {
		final BaseLib baselib;
		print(BaseLib baselib) {
			this.baselib = baselib;
		}
		public Varargs invoke(Varargs args) {
			LuaValue tostring = globals.get("tostring");
			for ( int i=1, n=args.narg(); i<=n; i++ ) {
				LuaString s = tostring.call( args.arg(i) ).strvalue();
				globals.STDOUT.print(s.tojstring());
			}
			return NONE;
		}
	}



	static final class tonumber extends LibFunction {
		public LuaValue call(LuaValue e) {
			return e.tonumber();
		}
		public LuaValue call(LuaValue e, LuaValue base) {
			if (base.isnil())
				return e.tonumber();
			final int b = base.checkint();
			if ( b < 2 || b > 36 )
				argerror(2, "base out of range");
			return e.checkstring().tonumber(b);
		}
	}


	static final class tostring extends LibFunction {
		public LuaValue call(LuaValue arg) {
			LuaValue h = arg.metatag(TOSTRING);
			if ( ! h.isnil() )
				return h.call(arg);
			LuaValue v = arg.tostring();
			if ( ! v.isnil() )
				return v;
			return valueOf(arg.tojstring());
		}
	}



	public Varargs loadStream(InputStream is, String chunkname, String mode, LuaValue env) {
		try {
			if ( is == null )
				return varargsOf(NIL, valueOf("not found: "+chunkname));
			return globals.load(is, chunkname, mode, env);
		} catch (Exception e) {
			return varargsOf(NIL, valueOf(e.getMessage()));
		}
	}
}
