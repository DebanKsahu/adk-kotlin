# Changelog

## [0.4.0](https://github.com/google/adk-kotlin/compare/v0.3.0...v0.4.0) (2026-06-22)


### Features

* add `AssetSkillSource` and introduce `SkillMdParsing` to reuse existing code ([cef3bad](https://github.com/google/adk-kotlin/commit/cef3bad84521fd5492d3cd38ba3b6d6839e75d6e))
* add `endInvocation()` to `CallbackContext` and `ToolContext` ([1bb5e3b](https://github.com/google/adk-kotlin/commit/1bb5e3ba0d38e8f2a08760769c209433d2d15065))
* add decoding of `response` wrapped confirmation response ([f123f6e](https://github.com/google/adk-kotlin/commit/f123f6e9bd3856f1520ad404a3f23b82b028e0e3))
* add filesystem-backed FileArtifactService ([affc9bf](https://github.com/google/adk-kotlin/commit/affc9bf4ace5fc385831d601facf18ea6af96658))
* add GitHub release-docs analyzer (Kotlin) ([d30f824](https://github.com/google/adk-kotlin/commit/d30f82451e736f9a4c1c06a13a045ca137587ca3))
* add LiteRT-LM model integration to Kotlin ADK ([2d9557c](https://github.com/google/adk-kotlin/commit/2d9557c047c9143debcebfb9adb299dedcb21494))
* Add maxSteps to Kotlin LlmAgent ([912379e](https://github.com/google/adk-kotlin/commit/912379ecdf2297ef69a502ffcae09455bc5b067f))
* Add outputKey to LlmAgent to save final text responses to session state ([1a16943](https://github.com/google/adk-kotlin/commit/1a169434b574946bbbf428b6e7671524e2770c2f))
* add Room storage primitives for Android sessions ([8b52926](https://github.com/google/adk-kotlin/commit/8b5292635337ec8fbccb75900e72139bde57cb2d))
* add Room-backed RoomSessionService for Android ([3371551](https://github.com/google/adk-kotlin/commit/3371551c31ed312015c815099401b69f2468118c))
* add Skills example to the examples module ([f1a88a4](https://github.com/google/adk-kotlin/commit/f1a88a4be3d571ce18154578b6ab5f0453bc5a21))
* add sliding-window event compaction strategy ([625e065](https://github.com/google/adk-kotlin/commit/625e065c16a7d6ef5771a4d2c5bc4a83f64efe16))
* Add support for outputSchema in LlmAgent ([1b94ec5](https://github.com/google/adk-kotlin/commit/1b94ec5e921a866f87b0c29a5b13bd82399e6047))
* Add UrlContextTool to ADK Kotlin ([3e7b94e](https://github.com/google/adk-kotlin/commit/3e7b94e01c06f29c83e08d7cce10f7f19e35a039))
* apply context-compaction summaries when building LLM request contents ([9c771ee](https://github.com/google/adk-kotlin/commit/9c771eef736f61c51f004729c0b91ec5c336db30))
* include thoughts and tool calls in compaction summaries, aligned with Python ([2c71902](https://github.com/google/adk-kotlin/commit/2c719026efcc2c9e4c6a5d1a7da586889201c6a7))
* move plugins and resumability configuration onto App ([c7ee0b7](https://github.com/google/adk-kotlin/commit/c7ee0b7408d99bf85284f5407fbaf87940848cfc))
* serialize the Event graph with kotlinx.serialization ([6146119](https://github.com/google/adk-kotlin/commit/614611985002e04390e5902a00e5e2cf56f75068))
* support constructing a Runner from an App ([e6f02e5](https://github.com/google/adk-kotlin/commit/e6f02e531230cd60a9635e7399023d665f5f9596))
* **telemetry:** align telemetry spans with Python ADK ([4035a3a](https://github.com/google/adk-kotlin/commit/4035a3ac0d0ff5a443dbe7a532096bb7c7f3594a))
* wire sliding-window context compaction into the runner ([65b383b](https://github.com/google/adk-kotlin/commit/65b383bf51f8932f67ae001ac533b8d3483c6b15))


### Bug Fixes

* add empty map as sentinel in `FunctionResponse` suppression in `LongRunningTool` ([ac8b9e6](https://github.com/google/adk-kotlin/commit/ac8b9e69452270ca8d2a2440e9eb720246728127))
* **agents:** emit all sub-agent events from LoopAgent before pausing or exiting ([ea10225](https://github.com/google/adk-kotlin/commit/ea102254d8573dfe7168de3d5c67a4c32366ed37))
* **agents:** keep the parent branch across agent runs and transfers ([8c41c8c](https://github.com/google/adk-kotlin/commit/8c41c8ce991c7786f7054084e74fa1e83d1aa1dd))
* exclude rewound invocations from sliding-window compaction ([fe96d97](https://github.com/google/adk-kotlin/commit/fe96d9761fcce62de01678013c7ae43b6c219eba))
* **models:** align Gemini tracking headers with other ADK SDKs ([1d61358](https://github.com/google/adk-kotlin/commit/1d613586571fe3e4f0e27838039330bab5da9785))
* **runners:** no-op when resuming an already-final invocation ([b8cb0bb](https://github.com/google/adk-kotlin/commit/b8cb0bbc3b68d49c2c3d92f2de3aa8adb3bd02bb))
* support custom name and description in @Tool annotation ([8bed997](https://github.com/google/adk-kotlin/commit/8bed9976a1b2874e515a97e1280f62b88f5f1097))
* **telemetry:** align execute_tool span name with Python/Java ADK ([501b618](https://github.com/google/adk-kotlin/commit/501b6183536588a0d5f41270bcd48c701c05f17e))
* **telemetry:** emit OpenTelemetry spans on Android ([0ccce75](https://github.com/google/adk-kotlin/commit/0ccce759be48b5afa0186c92f1acb543b8bd7e92))
* **telemetry:** set gen_ai.tool.type to the tool class name ([fe0cbff](https://github.com/google/adk-kotlin/commit/fe0cbff99d4d460a8abe5b5e6598cf56fd1aff4b))
* **telemetry:** use gcp.vertex.agent as the instrumentation scope name ([19de1c2](https://github.com/google/adk-kotlin/commit/19de1c2caca8600d4d052537fd88c073763d36a7))


### Documentation

* add a limitation remark about session persistence in Firebase ([8fc1d2e](https://github.com/google/adk-kotlin/commit/8fc1d2e35927353c7692a2ed2b38e0416de4738f))
* add an on-device Room session example app ([ce49924](https://github.com/google/adk-kotlin/commit/ce499247d1acfa3e02183786413446dc3879705c))
* **telemetry:** clarify captureMessageContent intentionally defaults off ([d7a5a14](https://github.com/google/adk-kotlin/commit/d7a5a148e52ba2eca3e40cd822bd449f33e2b882))

## [0.3.0](https://github.com/google/adk-kotlin/compare/v0.2.1-SNAPSHOT...v0.3.0) (2026-06-12)


### Features

* A2A Agent remote sample added ([ab34dd8](https://github.com/google/adk-kotlin/commit/ab34dd8507cb70aa89c13a173467abde71a28d57))
* Add AgentTransferDemoAgent for demonstrating agent-to-agent transfer ([00a02e2](https://github.com/google/adk-kotlin/commit/00a02e27b3cf875ba0cac42ed81ca11a749cf76c))
* Add GoogleSearchExample to the examples ([09bc61c](https://github.com/google/adk-kotlin/commit/09bc61c79e6755172948b7dcf308d3b7e8114577))
* Add HitlDemoAgent example ([dd75810](https://github.com/google/adk-kotlin/commit/dd758100d653a3c4098a295e139a0988d6592971))
* add Runner.rewindAsync to undo session state and artifacts ([96c6319](https://github.com/google/adk-kotlin/commit/96c63191fa43677a3ea2cd9b7b7467cd7befa098))
* Add TelemetryDemoAgent example ([d9ac998](https://github.com/google/adk-kotlin/commit/d9ac99877aa4d31ef32bf20a36d7ce56bc822561))
* introduce App data class for Kotlin ADK ([e86d9f6](https://github.com/google/adk-kotlin/commit/e86d9f68e0e78d7f7ef2711c6b4489198b9d1613))
* introduce EventCompaction and add it to EventActions ([aeb43b5](https://github.com/google/adk-kotlin/commit/aeb43b533c52299a59a815fc3d37021a7dd6d798))
* introduce EventSummarizer interface ([5fc6b14](https://github.com/google/adk-kotlin/commit/5fc6b147b090daa5cdd437df68a258400652015e))
* introduce LlmEventSummarizer ([9c50e12](https://github.com/google/adk-kotlin/commit/9c50e12ed108db0f47c4fef800394b8cde9449b9))


### Bug Fixes

* honor rewindBeforeInvocationId in HistoryRewriterProcessor ([33195bf](https://github.com/google/adk-kotlin/commit/33195bfe51828a8b7b6b49b7d85c57d96c7b80a6))
* make `NewFileSystemSource.kt` compatible with AndroidSDK 26+ ([04195c1](https://github.com/google/adk-kotlin/commit/04195c10ceb4da49ef7ae761365a75c68de9278d))
* Update tracing in GenaiPrompt: redact prompts and function calls ([a93e5d6](https://github.com/google/adk-kotlin/commit/a93e5d639c31c4dc760a308b53ec6e8a2d1d6310))

## [0.2.0](https://github.com/google/adk-kotlin/compare/v0.1.1...v0.2.0) (2026-05-26)


### Features

* Added structural agent demos: LoopAgentDemo, ParallelAgentDemo, and SequentialAgentDemo ([2a93053](https://github.com/google/adk-kotlin/commit/2a9305317766b094563cee13f7c3823c4738c62e))
* Add a new example agent demonstrating callbacks ([9183250](https://github.com/google/adk-kotlin/commit/918325061412ef62f32c1d958a5927328b9c9bfa))
* Add a new ReportGeneratorAgent example ([c6ea965](https://github.com/google/adk-kotlin/commit/c6ea96552ca259ff047a63cd04eac177ea8daac4))
* Add input schema validation to AgentTool ([3df572f](https://github.com/google/adk-kotlin/commit/3df572f86ef795682b92461e774dca5e8b837f5b))


### Bug Fixes

* exclude protobuf-java transitive dependency in android ([3b2100c](https://github.com/google/adk-kotlin/commit/3b2100c2efd67489f372ad1336cefd5ae7ee0ec0))
* **processor:** support Map&lt;String, Any&gt; and List&lt;Any&gt; as @Tool return types ([26c3f9d](https://github.com/google/adk-kotlin/commit/26c3f9d8231828c323e0329cb0d969f3670e322b))

## 0.1.0 (2026-05-19)

ADK Kotlin 0.1 release. Provides core features for building AI agents on JVM and Android, including:
* LLM agents, custom agents
* Multi - agent orchestration
* Function tools, Agent Skills, and long-running operations
* In-memory session and memory services
* Model integrations: 
  * Gemini on JVM/Android (Google GenAI SDK, Firebase AI),
  * On-device Gemini Nano and Gemma (ML Kit)
* ADK web UI interface

For full details, please visit the official documentation at https://adk.dev.

**Full Changelog**: https://github.com/google/adk-kotlin/commits/v0.1.0
