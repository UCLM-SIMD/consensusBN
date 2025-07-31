# consensusBN - Bayesian Network Fusion

[![CI](https://github.com/UCLM-SIMD/consensusBN/actions/workflows/ci.yml/badge.svg)](https://github.com/UCLM-SIMD/consensusBN/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-8%2B-blue)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

## Overview

`consensusBN` is a Java-based library for Bayesian Network Fusion. This project allows users to combine multiple Bayesian networks into a single consensus network, leveraging the power of consensus-based modeling techniques. The project is supported by a published paper [(link)](https://www.sciencedirect.com/science/article/abs/pii/S156625352030364X), titled "Efficient and accurate structural fusion of Bayesian networks."

![Bayesian Network Fusion](assets/bn_fusion.jpg)

## Features

- Combine multiple Bayesian networks into a consensus network.
- Support for various Bayesian network formats (e.g., BIF, XML, JSON).
- Flexible fusion strategies and customization options.
- Comprehensive documentation and examples for easy usage.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

### Installation

You can include `consensusBN` in your Maven project by adding the following dependency:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>consensusBN</artifactId>
    <version>1.0.0</version> <!-- Update with the latest version -->
</dependency>
```

### Usage
```

import com.example.consensusBN.*;

public class MyBNFusionApp {
    public static void main(String[] args) {
        // Your code here
    }
}
```

## Documentation

Visit the [Documentation](docs/) directory for detailed information on how to use `consensusBN`, including API reference, examples, and best practices.

## Contributing

We welcome contributions! If you'd like to contribute to `consensusBN`, please follow our [Contribution Guidelines](CONTRIBUTING.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- The authors of the original paper: José Miguel Puerta, Juan Ángel Aledo, José Antonio Gámez and Jorge D. Laborda
- [Tetrad project](www.phil.cmu.edu/tetrad)  

## Contact

For questions, suggestions, or feedback, please create an issue.

