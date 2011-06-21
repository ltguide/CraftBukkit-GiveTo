package ltguide.giveto;

class CommandException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CommandException(CommandState state) {
		super(state.Message);
	}
	
	public CommandException(CommandState state, Object... args) {
		super(state.format(args));
	}
}
