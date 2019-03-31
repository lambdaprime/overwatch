package id.util;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class for parsing command line.
 * Key-value arguments sent to HANDLERS.
 * Key only arguments sent to DEFAULT_HANDLER.
 * If default handler returns false we stop.
 * In case of wrong arguments exception is thrown.
 */
public class SmartArgs {

    private Map<String, Consumer<String>> handlers;
    private Function<String, Boolean> defaultHandler;

    public SmartArgs(Map<String, Consumer<String>> handlers, Function<String, Boolean> defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }

    public void parse(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            boolean expectValue = handlers.containsKey(args[i]);
            if (expectValue && i + 1 == args.length)
                throw new Exception();
            if (!expectValue && !defaultHandler.apply(args[i]))
                return;
            if (!expectValue)
                continue;
            handlers.get(args[i]).accept(args[i + 1]);
            i++;
        }
    }
}
