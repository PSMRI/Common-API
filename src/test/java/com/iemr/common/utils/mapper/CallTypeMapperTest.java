package com.iemr.common.utils.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.annotations.Expose;

/** Covers the Gson wrapper that only serialises {@code @Expose}-annotated call-type fields. */
class CallTypeMapperTest {

	static class Sample {
		@Expose
		String exposed;
		String hidden;
	}

	private final CallTypeMapper mapper = new CallTypeMapper();

	@Test
	@DisplayName("only annotated fields are read back from JSON")
	void onlyExposedFieldsAreDeserialized() {
		Sample sample = mapper.fromJson("{\"exposed\":\"kept\",\"hidden\":\"dropped\"}", Sample.class);

		assertThat(sample.exposed).isEqualTo("kept");
		assertThat(sample.hidden).isNull();
	}

	@Test
	@DisplayName("only annotated fields are written out")
	void onlyExposedFieldsAreSerialized() {
		Sample sample = new Sample();
		sample.exposed = "kept";
		sample.hidden = "dropped";

		assertThat(CallTypeMapper.gson().toJson(sample)).isEqualTo("{\"exposed\":\"kept\"}");
	}

	@Test
	@DisplayName("the Gson instance is shared between calls")
	void gsonInstanceIsShared() {
		assertThat(CallTypeMapper.gson()).isSameAs(CallTypeMapper.gson());
	}

	@Test
	@DisplayName("null JSON deserialises to null rather than failing")
	void nullJsonYieldsNull() {
		assertThat(mapper.fromJson(null, Sample.class)).isNull();
	}

	@Test
	@DisplayName("malformed JSON is rethrown so the caller sees the parse failure")
	void malformedJsonIsRethrown() {
		assertThatThrownBy(() -> mapper.fromJson("{not json", Sample.class)).isInstanceOf(RuntimeException.class);
	}
}
