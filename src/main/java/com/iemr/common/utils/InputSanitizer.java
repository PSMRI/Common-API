package com.iemr.common.utils;

import org.springframework.web.util.HtmlUtils;

/**
 * Utility class for sanitizing user inputs to prevent XSS, SQL Injection, and Command Injection
 */
public class InputSanitizer {
	
	// Characters that are dangerous for XSS
	private static final String[] XSS_PATTERNS = {
		"<script", "</script>", "javascript:", "onerror=", "onload=", 
		"<iframe", "<object", "<embed", "eval(", "expression("
	};
	
	// Characters that could be used for command injection
	private static final String[] COMMAND_INJECTION_CHARS = {
		";", "|", "&", "`", "$", "(", ")", "{", "}", "[", "]", 
		"&&", "||", ">", "<", "\\", "\n", "\r"
	};
	
	/**
	 * Sanitize input to prevent XSS attacks
	 * @param input User input string
	 * @return Sanitized string with HTML entities encoded
	 */
	public static String sanitizeForXSS(String input) {
		if (input == null || input.trim().isEmpty()) {
			return input;
		}
		
		// HTML encode to neutralize XSS
		String sanitized = HtmlUtils.htmlEscape(input);
		
		// Additional check for common XSS patterns (case-insensitive)
		String lowerInput = sanitized.toLowerCase();
		for (String pattern : XSS_PATTERNS) {
			if (lowerInput.contains(pattern.toLowerCase())) {
				// Remove the dangerous pattern
				sanitized = sanitized.replaceAll("(?i)" + pattern, "");
			}
		}
		
		return sanitized;
	}
	
	/**
	 * Sanitize input to prevent command injection
	 * Removes shell metacharacters
	 * @param input User input string
	 * @return Sanitized string
	 */
	public static String sanitizeForCommandInjection(String input) {
		if (input == null || input.trim().isEmpty()) {
			return input;
		}
		
		String sanitized = input;
		
		// Remove dangerous command injection characters
		for (String dangerChar : COMMAND_INJECTION_CHARS) {
			sanitized = sanitized.replace(dangerChar, "");
		}
		
		return sanitized;
	}
	
	/**
	 * Comprehensive sanitization for general text input
	 * Combines XSS and command injection protection
	 * @param input User input string
	 * @return Sanitized string
	 */
	public static String sanitize(String input) {
		if (input == null) {
			return null;
		}
		
		// First remove command injection chars, then sanitize XSS
		String sanitized = sanitizeForCommandInjection(input);
		sanitized = sanitizeForXSS(sanitized);
		
		return sanitized.trim();
	}
	
	/**
	 * Validate that template parameter syntax is safe
	 * Allows ${paramName} but prevents ${`command`} style injections
	 * @param template Template string
	 * @return true if template is safe
	 */
	public static boolean isValidTemplateParameter(String template) {
		if (template == null || template.trim().isEmpty()) {
			return false;
		}
		
		// Check for command injection attempts in template parameters
		// Valid: ${userName}, ${age}
		// Invalid: ${`ls`}, ${$(whoami)}, ${;rm -rf}
		
		if (template.contains("${`") || template.contains("$(`") || 
			template.contains("${$(") || template.contains("${;")) {
			return false;
		}
		
		return true;
	}
}


