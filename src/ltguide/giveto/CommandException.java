package ltguide.giveto;


class CommandException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CommandException(CommandMessage message) {
		super(message.toString());
	}
	
	public CommandException(CommandMessage message, Object... args) {
		super(message.toString(args));
	}
}
