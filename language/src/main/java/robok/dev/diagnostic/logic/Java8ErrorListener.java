package robok.dev.diagnostic.logic;

import android.util.Log;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import org.antlr.v4.runtime.*;

public class Java8ErrorListener extends BaseErrorListener {
    
    ErrorDiagnostico d;
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            // Custom error message for missing '{'
            
            // Posição inicial
                int startChar = token.getStartIndex();
                
                // Posição final (considerando o comprimento do texto do token)
                int endChar = token.getStopIndex();
            
            String erro = "line " + token.getLine() + ":" + token.getCharPositionInLine() + " " + msg;
            
             d.error(token.getLine(), startChar, endChar, erro);
            
          /*  if (msg.contains("missing '{'")) {
                // Obtain the previous token
                TokenStream tokens = ((Parser)recognizer).getInputStream();
                Token previousToken = tokens.get(token.getTokenIndex() - 1);
                System.err.printf(
                        "line %d:%d missing '{' after %s%n",
                        previousToken.getLine(),
                        previousToken.getCharPositionInLine() + previousToken.getText().length(),
                        previousToken.getText());

                // Posição inicial
                int startChar = previousToken.getStartIndex();
                
                // Posição final (considerando o comprimento do texto do token)
                int endChar = previousToken.getStopIndex();

                String erro = "line " + previousToken.getLine() + ":" + 
                   (previousToken.getCharPositionInLine() + previousToken.getText().length()) + 
                   " missing '{' after " + previousToken.getText();
                
               // d.error(previousToken.getLine(), previousToken.getCharPositionInLine() + previousToken.getText().length(), erro);
                
                d.error(previousToken.getLine(), startChar, endChar, erro);
            } else {
                String erro = "line " + token.getLine() + ":" + token.getCharPositionInLine() + " " + msg;
                d.error(token.getLine(), token.getStartIndex(), token.getStopIndex(), erro);
             //   d.error();
            }*/
        } else {
            Log.e("RobokErrorListener", "Error in find character from error");
             d.error(line, charPositionInLine, charPositionInLine + 1, "line " + line + ":" + charPositionInLine + " " + msg);
        }
        
    }

    
    
    
    public void getError(ErrorDiagnostico d){
        this.d = d;
    }
    
    public static interface ErrorDiagnostico{
        public void error(int line, int positionStart, int postionEnd, String msg);
    }
}



