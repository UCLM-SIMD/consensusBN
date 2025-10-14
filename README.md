# consensusBN - Bayesian Network Fusion Library

[![CI](https://github.com/UCLM-SIMD/consensusBN/actions/workflows/ci.yml/badge.svg)](https://github.com/UCLM-SIMD/consensusBN/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

## 🧠 Overview

`consensusBN` is a Java-based library for Bayesian Network Fusion. This project allows users to combine multiple Bayesian networks into a single consensus network, leveraging the power of consensus-based modeling techniques. The project is supported by a published paper [(link)](https://www.sciencedirect.com/science/article/abs/pii/S156625352030364X), titled "Efficient and accurate structural fusion of Bayesian networks."

![Bayesian Network Fusion](assets/bn_fusion.jpg)

## 🚀 Features

- 🔀 Merge multiple Bayesian networks into a single consensus structure.
- 📄 Support for BIF, XML, JSON formats.
- ⚙️ Customizable fusion strategies (e.g., Union, BES).
- 🧪 Includes tests and reproducible examples.

---

## 📦 Installation

### Requirements

- Java 17 or higher
- Maven 3.6 or higher

### Installation
You can include `consensusBN` to your Maven project by adding the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.jlaborda</groupId>
    <artifactId>consensusBN</artifactId>
    <version>v1.0.1</version>
</dependency>
```

### Usage
```

import es.uclm.i3a.simd.consensusBN.*

public class MyBNFusionApp {
    public static void main(String[] args) {
        // Creating list of shared nodes
        ArrayList<Node> sharedNodes = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            Node node = new GraphNode("Node" + i);
            sharedNodes.add(node);
        }
        // Setting seed
        RandomUtil.getInstance().setSeed(42);

        // Generating random DAGs
        for (int i = 0; i < sizeRandomDags; i++) {
            Dag randomDag = RandomGraph.randomDag(sharedNodes,0,50,19,19,19,true);
            randomDagsList.add(randomDag);
        }
        
        // Applying ConsensusUnion
        ConsensusUnion conDag = new ConsensusUnion(randomDagsList);
        ConsensusBES conBES = new ConsensusBES(randomDagsList);
        Graph gUnion = conDag.union();
        Graph gBES = conBES.fusion();

        System.out.println("Union: \n" + gUnion);
        System.out.println("BES: \n" + gUnion);
    }
}
```

## 📚  Documentation

Documentation will be available soon in the [Documentation](docs/) directory.
Stay tuned for:
- API reference
- Use-case examples
- Integration tips

## 🤝 Contributing

We welcome contributions! If you'd like to contribute to `consensusBN`, please follow our [Contribution Guidelines](CONTRIBUTING.md).

## 📖 How to Cite
If you use `consensusBN` in your research, please cite our [publication](https://www.sciencedirect.com/science/article/pii/S156625352030364X):

```
@article{PUERTA2021155,
title = {Efficient and accurate structural fusion of Bayesian networks},
journal = {Information Fusion},
volume = {66},
pages = {155-169},
year = {2021},
issn = {1566-2535},
doi = {https://doi.org/10.1016/j.inffus.2020.09.003},
url = {https://www.sciencedirect.com/science/article/pii/S156625352030364X},
author = {José M. Puerta and Juan A. Aledo and José A. Gámez and Jorge D. Laborda},
keywords = {Bayesian networks, Aggregation, Fusion, Consensus, Heuristic orders}
}
```


## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙌 Acknowledgments

- The authors of the original paper: José Miguel Puerta, Juan Ángel Aledo, José Antonio Gámez and Jorge D. Laborda
- [Tetrad project](www.phil.cmu.edu/tetrad)  

## Contact

For questions, suggestions, or feedback, please create an issue.

